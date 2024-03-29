package io.github.greyp9.irby.core.cron.impl;

import io.github.greyp9.arwo.core.date.DateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;
import io.github.greyp9.arwo.core.xml.ElementU;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({ "PMD.DoNotUseThreads", "unused" })
public class SleepRunnable extends CronRunnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public SleepRunnable(final String tab, final String name, final Date date, final Element element) {
        super(tab, name, date, element);
    }

    @Override
    public final void run() {
        final String className = getClass().getName();
        final String date = XsdDateU.toXSDZMillis(getDate());
        final String methodName = String.format("run(%s)", date);
        logger.entering(className, methodName + "::" + Thread.currentThread().getName());
        final String duration = ElementU.getAttribute(getElement(), Const.DURATION, DurationU.Const.ZERO_SECONDS);
        final Date dateUntil = DurationU.add(new Date(), DateU.Const.TZ_GMT, duration);
        final boolean interrupted = ThreadU.sleepUntil(dateUntil);
        logger.log(Level.FINE, String.format("[%s][%s][%s][%s]", Thread.currentThread().getName(),
                XsdDateU.toXSDZMillis(getDate()), duration, interrupted));
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String DURATION = "duration";  // i18n internal
    }
}
