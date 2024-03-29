package io.github.greyp9.irby.core.tcp.server;

import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.tcp.config.TCPConfig;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class TCPRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final TCPServer server;
    private final AtomicReference<String> reference;

    public static TCPRunnable create(final TCPConfig config,
                                     final AtomicReference<String> reference) {
        return new TCPRunnable(config, reference);
    }

    public TCPRunnable(final TCPConfig config,
                       final AtomicReference<String> reference) {
        this.server = new TCPServer(config, reference);
        this.reference = reference;
    }

    @Override
    public final void run() {
        final String methodName = String.format("run(%d)", server.getConfig().getPort());
        logger.entering(getClass().getSimpleName(), methodName);
        try {
            server.start();
            while (reference.get() == null) {
                server.accept();
            }
            server.stop();
        } catch (IOException e) {
            reference.compareAndSet(null, String.format("[%d][%s]", server.getConfig().getPort(), e.getMessage()));
        }
        MutexU.notifyAll(reference);
        logger.exiting(getClass().getSimpleName(), methodName);
    }
}
