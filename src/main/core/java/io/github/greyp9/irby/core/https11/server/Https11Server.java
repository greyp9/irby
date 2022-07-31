package io.github.greyp9.irby.core.https11.server;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.jce.KeyX;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.tls.context.TLSContext;
import io.github.greyp9.arwo.core.tls.context.TLSContextFactory;
import io.github.greyp9.arwo.core.tls.manage.TLSKeyManager;
import io.github.greyp9.arwo.core.tls.manage.TLSTrustManager;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.arwo.core.xed.extension.XedKey;
import io.github.greyp9.arwo.core.xsd.instance.TypeInstance;
import io.github.greyp9.arwo.core.xsd.model.XsdTypes;
import io.github.greyp9.irby.core.Irby;
import io.github.greyp9.irby.core.http11.dispatch.Http11Dispatcher;
import io.github.greyp9.irby.core.https11.config.Https11Config;
import io.github.greyp9.irby.core.https11.socket.Https11SocketRunnable;
import io.github.greyp9.irby.core.realm.Realms;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class Https11Server {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Https11Config config;
    private final Http11Dispatcher dispatcher;
    private final ExecutorService executorService;

    // lifecycle start/stop
    private ServerSocket serverSocket;

    public final Https11Config getConfig() {
        return config;
    }

    public Https11Server(final Https11Config config, final Realms realms, final ExecutorService executorService) {
        this.config = config;
        this.dispatcher = new Http11Dispatcher(config, realms);
        final String prefix = String.format("%s-%d", getClass().getSimpleName(), config.getPort());
        this.executorService = (config.isLocalExecutor()
                ? ExecutorServiceFactory.create(config.getThreads(), prefix) : executorService);
        this.serverSocket = null;
    }

    public final void start() throws IOException {
        dispatcher.register(config.getContexts());
        serverSocket = startServerSocket(config, logger);
        logger.info(String.format("Service [%s/%s] bound to port [%d]",
                config.getType(), config.getName(), config.getPort()));
    }

    public final void stop() throws IOException {
        dispatcher.unregister();
        if (config.isLocalExecutor()) {
            executorService.shutdownNow();
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public final void accept() throws IOException {
        try {
            final Socket socket = serverSocket.accept();
            executorService.execute(new Https11SocketRunnable(dispatcher, socket));
        } catch (SocketTimeoutException e) {
            //noinspection ResultOfMethodCallIgnored
            e.getClass();  // ignore; serverSocket.setSoTimeout()
        }
    }

    private static ServerSocket startServerSocket(final Https11Config config, final Logger logger) throws IOException {
        try {
            // server SSL params, trusted client SSL params
            final KeyX keyX = getKey();
            final TLSKeyManager keyManager = getKeyManager(config, getKey());
            final X509Certificate x509Certificate = Objects.requireNonNull(keyManager.getCertificate());
            logger.info(String.format("Service certificate expires: %s",
                    XsdDateU.toXSDZMillis(x509Certificate.getNotAfter())));
            final TLSTrustManager trustManager = (config.isNeedClientAuth() ? getTrustManager(config, keyX) : null);
            // context implements TLS server with optional client X.509 authentication
            final TLSContext context = new TLSContext(keyManager, trustManager, config.getProtocol());
            final SSLServerSocketFactory ssf = context.getServerSocketFactory();
            final ServerSocket serverSocket = ssf.createServerSocket(config.getPort());
            serverSocket.setSoTimeout((int) DurationU.Const.ONE_SECOND_MILLIS);
            return serverSocket;
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private static TLSKeyManager getKeyManager(final Https11Config config, final KeyX keyX)
            throws GeneralSecurityException, IOException {
        final String keyStorePass = config.getKeyStorePass();
        final String keyStorePassClear = keyX.hasKey() ? keyX.unprotect(keyStorePass) : keyStorePass;
        return new TLSContextFactory().getKeyManager(
                config.getKeyStoreType(), config.getKeyStoreFile(), keyStorePassClear.toCharArray());
    }

    private static TLSTrustManager getTrustManager(final Https11Config config, final KeyX keyX)
            throws GeneralSecurityException, IOException {
        final char[] clientTrustPass = keyX.unprotect(config.getClientTrustPass()).toCharArray();
        return new TLSContextFactory().getTrustManager(
                config.getClientTrustType(), config.getClientTrustFile(), clientTrustPass);
    }

    private static KeyX getKey() throws GeneralSecurityException, IOException {
        final KeyStore keyStore = (KeyStore) AppNaming.lookup(App.Secret.CONTEXT, App.Secret.NAME);
        final Key keyApp = keyStore.getKey(Irby.App.URI, SystemU.userDir().toCharArray());
        final XsdTypes xsdTypes = new XsdTypes(ResourceU.resolve(Irby.App.XSD));
        final TypeInstance typeInstance = xsdTypes.getElementType(Irby.App.QNAME.toString())
                .getInstance("https11").getInstance("keyStorePass");
        return XedKey.getKeyAES(keyApp, typeInstance);
    }
}
