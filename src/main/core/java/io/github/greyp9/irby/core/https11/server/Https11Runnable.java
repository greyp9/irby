package io.github.greyp9.irby.core.https11.server;

import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.https11.config.Https11Config;
import io.github.greyp9.irby.core.realm.Realms;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class Https11Runnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Https11Server server;
    private final AtomicReference<String> reference;

    public static Https11Runnable create(final Https11Config config, final Realms realms,
                                         final ExecutorService executorService,
                                         final AtomicReference<String> reference) {
        return new Https11Runnable(config, realms, executorService, reference);
    }

    public Https11Runnable(final Https11Config config, final Realms realms,
                           final ExecutorService executorService,
                           final AtomicReference<String> reference) {
        this.server = new Https11Server(config, realms, executorService);
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
