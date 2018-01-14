package io.github.greyp9.irby.core.proxy.server;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.http.header.Host;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.irby.core.proxy.config.ProxyConfig;
import io.github.greyp9.irby.core.proxy.socket.ProxySocketRunnable;

import javax.net.ServerSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class ProxyServer {
    private final ProxyConfig config;
    private final ExecutorService executorService;
    private final File folder;
    private final AtomicReference<String> reference;

    // lifecycle start/stop
    private ServerSocket serverSocket;


    public final ProxyConfig getConfig() {
        return config;
    }

    public ProxyServer(final ProxyConfig config,
                       final ExecutorService executorService,
                       final AtomicReference<String> reference) {
        this.config = config;
        final String prefix = String.format("%s-%d", getClass().getSimpleName(), config.getPort());
        this.executorService = (config.isLocalExecutor() ?
                ExecutorServiceFactory.create(config.getThreads(), prefix) : executorService);
        this.folder = FileU.toFileIfExists(config.getFolder());
        this.reference = reference;
        this.serverSocket = null;
    }

    public final void start() throws IOException {

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
            executorService.execute(new ProxySocketRunnable(
                    socket, new Host(config.getHost()), executorService, folder, reference));
        } catch (SocketTimeoutException e) {
            e.getClass();  // ignore; serverSocket.setSoTimeout()
        }
    }

    private static ServerSocket startServerSocket(final ProxyConfig config) throws IOException {
        final ServerSocketFactory ssf = ServerSocketFactory.getDefault();
        final ServerSocket serverSocket = ssf.createServerSocket(config.getPort());
        serverSocket.setSoTimeout((int) DurationU.Const.ONE_SECOND_MILLIS);
        return serverSocket;
    }
}
