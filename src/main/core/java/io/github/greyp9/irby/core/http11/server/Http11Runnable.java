package io.github.greyp9.irby.core.http11.server;

import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.http11.config.Http11Config;
import io.github.greyp9.irby.core.realm.Realms;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class Http11Runnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Http11Server server;
    private final AtomicReference<String> reference;

    public static Http11Runnable create(final Http11Config config, final Realms realms,
                                        final ExecutorService executorService,
                                        final AtomicReference<String> reference) {
        return new Http11Runnable(config, realms, executorService, reference);
    }

    public Http11Runnable(final Http11Config config, final Realms realms,
                          final ExecutorService executorService,
                          final AtomicReference<String> reference) {
        this.server = new Http11Server(config, realms, executorService);
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
        } catch (IOException e) {
            reference.compareAndSet(null, e.getMessage());
        }
        MutexU.notifyAll(reference);
        logger.exiting(getClass().getSimpleName(), methodName);
    }
}
