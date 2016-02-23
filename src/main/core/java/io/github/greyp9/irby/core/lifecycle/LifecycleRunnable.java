package io.github.greyp9.irby.core.lifecycle;

import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.AvoidFinalLocalVariable" })
public class LifecycleRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final String propertyKey;
    private final AtomicReference<String> reference;
    private final long interval;

    public LifecycleRunnable(final String propertyKey, final AtomicReference<String> reference, final long interval) {
        this.propertyKey = propertyKey;
        this.reference = reference;
        this.interval = interval;
    }

    @Override
    public final void run() {
        final String methodName = "run()";
        logger.entering(getClass().getSimpleName(), methodName);
        final Properties properties = System.getProperties();
        while (reference.get() == null) {
            monitor(properties);
        }
        MutexU.notifyAll(reference);
        logger.exiting(getClass().getSimpleName(), methodName);
    }

    private void monitor(final Properties properties) {
        final String value = properties.getProperty(propertyKey);
        if (value == null) {
            ThreadU.sleepMillis(interval);
        } else {
            reference.compareAndSet(null, value);
        }
    }
}
