package io.github.greyp9.irby.core.lifecycle;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.http.HttpArguments;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.AvoidFinalLocalVariable" })
public class LifecycleRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final String propertyKey;
    private final AtomicReference<String> reference;
    private final long interval;

    private Date dateScheduled;
    private String referenceScheduled;

    public LifecycleRunnable(final String propertyKey, final AtomicReference<String> reference, final long interval) {
        this.propertyKey = propertyKey;
        this.reference = reference;
        this.interval = interval;
    }

    @Override
    public final void run() {
        final String methodName = "run()";  // i18n trace
        logger.entering(getClass().getSimpleName(), methodName);
        logger.fine(String.format("listening for [%s]", propertyKey));
        final Properties properties = System.getProperties();
        while (reference.get() == null) {
            monitor(properties);
        }
        logger.fine(String.format("notified [%s]", propertyKey));
        MutexU.notifyAll(reference);
        logger.exiting(getClass().getSimpleName(), methodName);
    }

    private void monitor(final Properties properties) {
        final String value = properties.getProperty(propertyKey);
        if (value == null) {
            ThreadU.sleepMillis(interval);
        } else {
            logger.info(value);
            process(value);
            properties.remove(propertyKey);
        }
        if (this.dateScheduled != null && this.dateScheduled.compareTo(new Date()) <= 0) {
            reference.compareAndSet(null, referenceScheduled.trim());
        }
    }

    private void process(final String value) {
        final NameTypeValues arguments = HttpArguments.toArguments(value);
        this.dateScheduled = Value.defaultOnNull(
                XsdDateU.fromXSDZ(arguments.getValue(App.Settings.DATE_SCHEDULED)), new Date());
        this.referenceScheduled = value;
    }
}
