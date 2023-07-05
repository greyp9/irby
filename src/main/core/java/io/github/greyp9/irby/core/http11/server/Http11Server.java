package io.github.greyp9.irby.core.http11.server;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.irby.core.http11.config.Http11Config;
import io.github.greyp9.irby.core.http11.dispatch.Http11Dispatcher;
import io.github.greyp9.irby.core.http11.socket.Http11SocketRunnable;
import io.github.greyp9.irby.core.realm.Realms;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class Http11Server {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Http11Config config;
    private final Http11Dispatcher dispatcher;
    private final ExecutorService executorService;

    // lifecycle start/stop
    private ServerSocket serverSocket;

    public final Http11Config getConfig() {
        return config;
    }

    public Http11Server(final Http11Config config, final Realms realms, final ExecutorService executorService) {
        this.config = config;
        this.dispatcher = new Http11Dispatcher(config, realms);
        final String prefix = String.format("%s-%d", getClass().getSimpleName(), config.getPort());
        this.executorService = (config.isLocalExecutor()
                ? ExecutorServiceFactory.create(config.getThreads(), prefix) : executorService);
        this.serverSocket = null;
    }

    public final void start() throws IOException {
        dispatcher.register(config.getContexts());
        serverSocket = startSocket(config);
        logger.info(String.format("Service [%s] bound to host [%s], TCP port [%d]",
                config.getName(), config.getHost(), config.getPort()));
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
            executorService.execute(new Http11SocketRunnable(dispatcher, socket));
        } catch (SocketTimeoutException e) {
            e.getClass();  // ignore; serverSocket.setSoTimeout()
        }
    }

    private static ServerSocket startSocket(final Http11Config config) throws IOException {
        final String host = config.getHost();
        final InetAddress inetAddress = host == null ? null : InetAddress.getByName(host);
        final ServerSocketFactory ssf = ServerSocketFactory.getDefault();
        final ServerSocket serverSocket = ssf.createServerSocket(config.getPort(), 0, inetAddress);
        serverSocket.setSoTimeout((int) DurationU.Const.ONE_SECOND_MILLIS);
        return serverSocket;
    }
}
