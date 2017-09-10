package io.github.greyp9.irby.core.cron.impl;

import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.lang.StringU;
import io.github.greyp9.arwo.core.xml.ElementU;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({ "unused" })
public class GenericRunnable extends CronRunnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public GenericRunnable(final Date date, final Element element) {
        super(date, element);
    }

    @Override
    public final void run() {
        final String className = getClass().getName();
        final String date = XsdDateU.toXSDZMillis(getDate());
        final String methodName = String.format("run(%s)", date);
        logger.entering(className, methodName);
        final String runnableClassName = ElementU.getAttribute(getElement(), Const.TYPE);
        final String parameters = ElementU.getAttribute(getElement(), Const.PARAMETERS);
        final String[] parametersArray = StringU.tokenize(parameters, StringU.Const.WHITESPACE);
        try {
            final Class<?> c = Class.forName(runnableClassName);
            final Constructor<?> constructor = c.getConstructor(String[].class);
            Runnable runnable = (Runnable) constructor.newInstance((Object) parametersArray);
            runnable.run();
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String PARAMETERS = "parameters";  // i18n internal
        private static final String TYPE = "type";  // i18n internal
    }
}
