package io.github.greyp9.irby.core.cron.factory;

import io.github.greyp9.irby.core.cron.impl.CronRunnable;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.logging.Logger;

@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.AvoidCatchingThrowable" })
public class JobFactory {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public final Runnable getRunnable(final String className, final Element element,
                                      final String tab, final String name, final Date date) {
        Runnable runnable = null;
        try {
            final Class<?> c = Class.forName(className);
            final Type type = c.getGenericSuperclass();
            final Class<?> s = (type instanceof Class<?>) ? ((Class<?>) type) : null;
            if (CronRunnable.class.equals(s)) {
                final Constructor<?> ctor = c.getConstructor(String.class, String.class, Date.class, Element.class);
                runnable = (Runnable) ctor.newInstance(tab, name, date, element);
            } else if (Runnable.class.equals(s)) {
                final Constructor<?> ctor = c.getConstructor();
                runnable = (Runnable) ctor.newInstance();
            }
        } catch (Throwable e) {
            logger.severe(e.getMessage());
        }
        return runnable;
    }
}
