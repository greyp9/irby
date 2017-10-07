package io.github.greyp9.irby.core.cron.impl;

import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.xml.ElementU;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({ "PMD.DoNotUseThreads", "unused" })
public class ArgumentsRunnable extends CronRunnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public ArgumentsRunnable(final String name, final Date date, final Element element) {
        super(name, date, element);
    }

    @Override
    public final void run() {
        final String className = getClass().getName();
        final String date = XsdDateU.toXSDZMillis(getDate());
        final String methodName = String.format("run(%s)", date);
        logger.entering(className, methodName);
        final String arguments = ElementU.getAttribute(getElement(), Const.ARGS);
        logger.log(Level.FINEST, String.format("[%s][%s]", XsdDateU.toXSDZMillis(getDate()), arguments));
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String ARGS = "args";  // i18n internal
    }
}
