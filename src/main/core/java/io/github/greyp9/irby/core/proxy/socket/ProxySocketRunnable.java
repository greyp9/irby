package io.github.greyp9.irby.core.proxy.socket;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.http.header.Host;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class ProxySocketRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Socket socket;
    private final Host host;
    private final ExecutorService executorService;
    private final AtomicReference<String> reference;

    public ProxySocketRunnable(final Socket socket, final Host host, final ExecutorService executorService,
                               final AtomicReference<String> reference) {
        this.socket = socket;
        this.host = host;
        this.executorService = executorService;
        this.reference = reference;
    }

    @Override
    public final void run() {
        final String className = getClass().getSimpleName();
        final String clientAddress = socket.getInetAddress().getHostAddress();
        final String methodName = String.format("run(%s,%s)", clientAddress, host.toString());
        try {
            logger.entering(className, methodName);
            doSocket();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            logger.exiting(className, methodName);
        }
    }

    private void doSocket() throws IOException {
        final Socket socketClient = this.socket;
        try {
            socketClient.setSoTimeout((int) DurationU.Const.HUNDRED_MILLIS);
            doSocket(socketClient);
        } finally {
            socketClient.close();
        }
    }

    private void doSocket(final Socket socketClient) throws IOException {
        final Socket socketServer = new Socket(host.getHost(), host.getPort());
        try {
            socketServer.setSoTimeout((int) DurationU.Const.HUNDRED_MILLIS);
            doSocket(socketClient, socketServer);
        } finally {
            socketServer.close();
        }
    }

    private void doSocket(final Socket socketClient, final Socket socketServer) throws IOException {
        final AtomicReference<String> referenceSocket = new AtomicReference<String>();
        final InputStream isC = socketClient.getInputStream();
        final OutputStream osS = socketServer.getOutputStream();
        final InputStream isS = socketServer.getInputStream();
        final OutputStream osC = socketClient.getOutputStream();
        executorService.execute(new ProxyStreamRunnable("c2s", isC, osS, referenceSocket));  // i18n internal
        executorService.execute(new ProxyStreamRunnable("s2c", isS, osC, referenceSocket));  // i18n internal
        while ((reference.get() == null) && (referenceSocket.get() == null)) {
            ThreadU.sleepMillis(DurationU.Const.HUNDRED_MILLIS);
        }
        referenceSocket.compareAndSet(null, getClass().getName());
    }
}
