package io.github.greyp9.irby.core.proxy.server;

import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.proxy.config.ProxyConfig;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class ProxyRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final ProxyServer server;
    private final AtomicReference<String> reference;

    public static ProxyRunnable create(final ProxyConfig config,
                                       final ExecutorService executorService,
                                       final AtomicReference<String> reference) {
        return new ProxyRunnable(config, executorService, reference);
    }

    public ProxyRunnable(final ProxyConfig config,
                         final ExecutorService executorService,
                         final AtomicReference<String> reference) {
        this.server = new ProxyServer(config, executorService, reference);
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
            logger.log(Level.SEVERE, e.getMessage(), e);
            // should component failure cause application failure?  if so, compareAndSet() signals process
            //reference.compareAndSet(null, String.format("[%d][%s]", server.getConfig().getPort(), e.getMessage()));
        }
        MutexU.notifyAll(reference);
        logger.exiting(getClass().getSimpleName(), methodName);
    }
}
