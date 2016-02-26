package io.github.greyp9.irby.core.proxys.socket;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.http.header.Host;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;
import io.github.greyp9.irby.core.proxy.socket.ProxyStreamRunnable;
import io.github.greyp9.irby.core.proxys.config.ProxysConfig;

import javax.net.SocketFactory;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class ProxysSocketRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Socket socket;
    private final SocketFactory socketFactory;
    private final ProxysConfig config;
    private final Host host;
    private final ExecutorService executorService;

    public ProxysSocketRunnable(final Socket socket, final SocketFactory socketFactory,
                                final ProxysConfig config, final ExecutorService executorService) {
        this.socket = socket;
        this.socketFactory = socketFactory;
        this.config = config;
        this.host = new Host(config.getHost());
        this.executorService = executorService;
    }

    @Override
    public final void run() {
        final String className = getClass().getSimpleName();
        final String clientAddress = socket.getInetAddress().getHostAddress();
        final String methodName = String.format("run(%s,%s)", clientAddress, host.toString());
        try {
            logger.entering(className, methodName);
            doSocket();
        } catch (SSLHandshakeException e) {
            final String message = String.format("%s/%s", socket.getInetAddress().getHostAddress(), e.getMessage());
            logger.log(Level.INFO, message);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            logger.exiting(className, methodName);
        }
    }

    private void doSocket() throws IOException {
        final Socket socketClient = augmentSSLSocket(config, socket);
        try {
            socketClient.setSoTimeout((int) DurationU.Const.TENTH_SECOND_MILLIS);
            doSocket(socketClient);
        } finally {
            socketClient.close();
        }
    }

    private static Socket augmentSSLSocket(final ProxysConfig config, final Socket socket) {
        if (config.isNeedClientAuth()) {
            final SSLSocket sslSocket = (SSLSocket) socket;
            sslSocket.setNeedClientAuth(true);
        }
        return socket;
    }

    private void doSocket(final Socket socketClient) throws IOException {
        final Socket socketServer = socketFactory.createSocket(host.getHost(), host.getPort());
        try {
            socketServer.setSoTimeout((int) DurationU.Const.TENTH_SECOND_MILLIS);
            doSocket(socketClient, socketServer);
        } finally {
            socketServer.close();
        }
    }

    private void doSocket(final Socket socketClient, final Socket socketServer) throws IOException {
        final AtomicReference<String> reference = new AtomicReference<String>();
        final InputStream isC = socketClient.getInputStream();
        final OutputStream osS = socketServer.getOutputStream();
        final InputStream isS = socketServer.getInputStream();
        final OutputStream osC = socketClient.getOutputStream();
        executorService.execute(new ProxyStreamRunnable("c2s", isC, osS, reference));  // i18n internal
        executorService.execute(new ProxyStreamRunnable("s2c", isS, osC, reference));  // i18n internal
        while (reference.get() == null) {
            ThreadU.sleepMillis(DurationU.Const.TENTH_SECOND_MILLIS);
        }
    }
}