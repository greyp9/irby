package io.github.greyp9.irby.core.https11.server;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.tls.context.TLSContext;
import io.github.greyp9.arwo.core.tls.manage.TLSKeyManager;
import io.github.greyp9.arwo.core.tls.manage.TLSTrustManager;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.irby.core.http11.dispatch.Http11Dispatcher;
import io.github.greyp9.irby.core.https11.config.Https11Config;
import io.github.greyp9.irby.core.https11.socket.Https11SocketRunnable;
import io.github.greyp9.irby.core.realm.Realms;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;

public class Https11Server {
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
        this.executorService = (config.isLocalExecutor() ?
                ExecutorServiceFactory.create(config.getThreads(), getClass().getSimpleName()) : executorService);
        this.serverSocket = null;
    }

    public final void start() throws IOException {
        dispatcher.register(config.getContexts());
        serverSocket = startSocket(config);
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
            e.getClass();  // ignore; serverSocket.setSoTimeout()
        }
    }

    private static ServerSocket startSocket(final Https11Config config) throws IOException {
        try {
            // server SSL params
            final TLSKeyManager keyManager = getKeyManager(config);
            // trusted client SSL params
            final TLSTrustManager trustManager = (config.isNeedClientAuth() ? getTrustManager(config) : null);
            // context
            final TLSContext context = new TLSContext(keyManager, trustManager, config.getProtocol());
            final SSLServerSocketFactory ssf = context.getServerSocketFactory();
            final ServerSocket serverSocket = ssf.createServerSocket(config.getPort());
            serverSocket.setSoTimeout((int) DurationU.Const.ONE_SECOND_MILLIS);
            return serverSocket;
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private static TLSKeyManager getKeyManager(final Https11Config config)
            throws GeneralSecurityException, IOException {
        final KeyStore keyStore = KeyStore.getInstance(config.getKeyStoreType());
        final File keyStoreFile = new File(config.getKeyStoreFile());
        final char[] password = config.getKeyStorePass().toCharArray();
        keyStore.load(new FileInputStream(keyStoreFile), password);
        return new TLSKeyManager(keyStore, password);
    }

    private static TLSTrustManager getTrustManager(final Https11Config config)
            throws GeneralSecurityException, IOException {
        final KeyStore keyStore = KeyStore.getInstance(config.getTrustStoreType());
        final File keyStoreFile = new File(config.getTrustStoreFile());
        final char[] password = config.getTrustStorePass().toCharArray();
        keyStore.load(new FileInputStream(keyStoreFile), password);
        return new TLSTrustManager(keyStore);
    }
}
