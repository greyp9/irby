package io.github.greyp9.irby.core.proxys.server;

import io.github.greyp9.arwo.core.cer.CertificateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.tls.context.TLSContext;
import io.github.greyp9.arwo.core.tls.manage.TLSKeyManager;
import io.github.greyp9.arwo.core.tls.manage.TLSTrustManager;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.irby.core.proxys.config.ProxysConfig;
import io.github.greyp9.irby.core.proxys.socket.ProxysSocketRunnable;

import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;

public class ProxysServer {
    private final ProxysConfig config;
    private final ExecutorService executorService;

    // lifecycle start/stop
    private SocketFactory socketFactory;
    private ServerSocket serverSocket;

    public final ProxysConfig getConfig() {
        return config;
    }

    public ProxysServer(final ProxysConfig config, final ExecutorService executorService) {
        this.config = config;
        final String prefix = String.format("%s-%d", getClass().getSimpleName(), config.getPort());
        this.executorService = (config.isLocalExecutor() ?
                ExecutorServiceFactory.create(config.getThreads(), prefix) : executorService);
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
            executorService.execute(new ProxysSocketRunnable(socket, socketFactory, config, executorService));
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

    private static TLSKeyManager getKeyManager(final ProxysConfig config)
            throws GeneralSecurityException, IOException {
        final KeyStore keyStore = KeyStore.getInstance(config.getKeyStoreType());
        final File keyStoreFile = new File(config.getKeyStoreFile());
        final char[] password = config.getKeyStorePass().toCharArray();
        keyStore.load(new FileInputStream(keyStoreFile), password);
        return new TLSKeyManager(keyStore, password);
    }

    private static TLSTrustManager getTrustManagerClient(final ProxysConfig config)
            throws GeneralSecurityException, IOException {
        final String clientTrustType = config.getClientTrustType();
        final KeyStore keyStore = KeyStore.getInstance(clientTrustType);
        final File keyStoreFile = new File(config.getClientTrustFile());
        final char[] password = config.getClientTrustPass().toCharArray();
        keyStore.load(new FileInputStream(keyStoreFile), password);
        return new TLSTrustManager(keyStore);
    }

    private static SocketFactory getSocketFactory(final ProxysConfig config) throws IOException {
        try {
            final TLSTrustManager trustManager = getTrustManagerServer(config);
            final TLSContext context = new TLSContext(null, trustManager, config.getProtocol());
            return context.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private static TLSTrustManager getTrustManagerServer(final ProxysConfig config)
            throws GeneralSecurityException, IOException {
        final File certificateFile = new File(config.getServerTrustFile());
        final X509Certificate certificate = CertificateU.toX509(StreamU.read(certificateFile));
        return new TLSTrustManager(new X509Certificate[] { certificate });
    }
}
