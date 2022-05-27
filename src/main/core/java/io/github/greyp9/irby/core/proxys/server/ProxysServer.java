package io.github.greyp9.irby.core.proxys.server;

import io.github.greyp9.arwo.core.cer.CertificateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.header.Host;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.tls.context.TLSContext;
import io.github.greyp9.arwo.core.tls.context.TLSContextFactory;
import io.github.greyp9.arwo.core.tls.manage.TLSKeyManager;
import io.github.greyp9.arwo.core.tls.manage.TLSTrustManager;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.irby.core.proxys.config.ProxysConfig;
import io.github.greyp9.irby.core.proxys.socket.ProxysSocketRunnable;

import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class ProxysServer {
    private final ProxysConfig config;
    private final ExecutorService executorService;
    private final File folder;
    private final AtomicReference<String> reference;

    // lifecycle start/stop
    private SocketFactory socketFactory;
    private ServerSocket serverSocket;

    public final ProxysConfig getConfig() {
        return config;
    }

    public ProxysServer(final ProxysConfig config,
                        final ExecutorService executorService,
                        final AtomicReference<String> reference) {
        this.config = config;
        final String prefix = String.format("%s-%d", getClass().getSimpleName(), config.getPort());
        this.executorService = (config.isLocalExecutor()
                ? ExecutorServiceFactory.create(config.getThreads(), prefix) : executorService);
        this.folder = FileU.toFileIfExists(config.getFolder());
        if (!Value.isEmpty(config.getFolder()) && (this.folder == null)) {
            final Logger logger = Logger.getLogger(getClass().getName());
            logger.warning(Value.join(Http.Token.SLASH,
                    FileNotFoundException.class.getSimpleName(), config.getFolder()));
        }
        this.reference = reference;
        this.serverSocket = null;
    }

    public final void start() throws IOException {
        socketFactory = getSocketFactory(config);
        serverSocket = startServerSocket(config);
    }

    public final void stop() throws IOException {
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
            executorService.execute(new ProxysSocketRunnable(
                    socket, new Host(config.getHost()), executorService, folder, reference, config, socketFactory));
        } catch (SocketTimeoutException e) {
            e.getClass();  // ignore; serverSocket.setSoTimeout()
        }
    }

    private static ServerSocket startServerSocket(final ProxysConfig config) throws IOException {
        try {
            // server SSL params
            final TLSKeyManager keyManager = getKeyManager(config);
            // trusted client SSL params
            final TLSTrustManager trustManager = (config.isNeedClientAuth() ? getTrustManagerClient(config) : null);
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

    private static TLSKeyManager getKeyManager(final ProxysConfig config)
            throws GeneralSecurityException, IOException {
        // the TLS key used by the proxy server to authenticate itself to incoming connections [@ApplicationConfig]
        return new TLSContextFactory().getKeyManager(config.getKeyStoreType(),
                config.getKeyStoreFile(), config.getKeyStorePass().toCharArray());
    }

    private static TLSTrustManager getTrustManagerClient(final ProxysConfig config)
            throws GeneralSecurityException, IOException {
        // the TLS certificate used by the proxy server to authenticate incoming connections [@ApplicationConfig]
        return new TLSContextFactory().getTrustManager(config.getClientTrustType(),
                config.getClientTrustFile(), config.getClientTrustPass().toCharArray());
    }

    private static SocketFactory getSocketFactory(final ProxysConfig config) throws IOException {
        try {
            final TLSTrustManager trustManager = getTrustManagerServer(config);
            // context implements TLS client with optional server certificate pinning
            final TLSContext context = new TLSContext(null, trustManager, config.getProtocol());
            return context.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private static TLSTrustManager getTrustManagerServer(final ProxysConfig config)
            throws GeneralSecurityException, IOException {
        TLSTrustManager trustManager = null;  // default is to handle by delegating to builtin trust CAs
        // the TLS certificate used by the proxy to authenticate the server on outgoing connections [@ProxysServer]
        final File certificateFile = FileU.toFileIfExists(config.getServerTrustFile());
        if (certificateFile != null) {
            final X509Certificate certificate = CertificateU.toX509(StreamU.read(certificateFile));
            trustManager = new TLSTrustManager(new X509Certificate[] { certificate });
        }
        return trustManager;
    }
}
