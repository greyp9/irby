package io.github.greyp9.irby.core.tcp.server;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.irby.core.tcp.config.TCPConfig;
import io.github.greyp9.irby.core.tcp.socket.TCPSocketRunnable;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class TCPServer {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final TCPConfig config;
    private final ExecutorService executorService;
    private final AtomicReference<String> reference;

    private ServerSocket serverSocket;

    public final TCPConfig getConfig() {
        return config;
    }

    public TCPServer(final TCPConfig config,
                     final AtomicReference<String> reference) {
        this.config = config;
        final String prefix = String.format("%s-%d", getClass().getSimpleName(), config.getPort());
        this.executorService = ExecutorServiceFactory.create(config.getThreads(), prefix);
        this.reference = reference;
        this.serverSocket = null;
    }

    public final void start() throws IOException {
        serverSocket = startServerSocket(config);
        logger.info(String.format("Service [%s] bound to host [%s], TCP port [%d], reference [%s]",
                config.getName(), config.getHost(), config.getPort(), reference.getClass().getName()));
    }

    public final void stop() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public final void accept() throws IOException {
        try {
            executorService.execute(new TCPSocketRunnable(config, serverSocket.accept()));
        } catch (SocketTimeoutException e) {
            logger.finest(e.getMessage());
        }
    }

    private static ServerSocket startServerSocket(final TCPConfig config) throws IOException {
        final String host = config.getHost();
        final InetAddress inetAddress = host == null ? null : InetAddress.getByName(host);
        final ServerSocketFactory ssf = ServerSocketFactory.getDefault();
        final ServerSocket serverSocket = ssf.createServerSocket(config.getPort(), 0, inetAddress);
        serverSocket.setSoTimeout((int) DurationU.Const.ONE_SECOND_MILLIS);
        return serverSocket;
    }
}
