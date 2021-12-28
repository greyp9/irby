package io.github.greyp9.irby.core.context.factory;

import io.github.greyp9.arwo.core.lifecycle.core.Disposable;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.irby.core.context.config.ContextConfig;
import io.github.greyp9.irby.core.context.config.ContextObject;

import javax.naming.Context;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ContextFactory {

    /**
     * Constructor.
     * https://checkstyle.sourceforge.io/config_design.html#HideUtilityClassConstructor
     */
    private ContextFactory() {
    }

    public static Context create(final ContextConfig contextConfig) {
        final Context context = AppNaming.createSubcontext(contextConfig.getName());
        for (ContextObject object : contextConfig.getObjects()) {
            AppNaming.bind(context, object.getName(), create(object));
        }
        return context;
    }

    private static Object create(final ContextObject objectConfig) {
        Logger logger = Logger.getLogger(ContextFactory.class.getName());
        final ArrayList<String> parameters = new ArrayList<String>(objectConfig.getParameters());
        parameters.add(0, objectConfig.getName());
        final String[] parametersArray = parameters.toArray(new String[parameters.size()]);
        Object object = null;
        try {
            final Class<?> c = Class.forName(objectConfig.getType());
            final Constructor<?> constructor = c.getConstructor(String[].class);
            object = constructor.newInstance((Object) parametersArray);
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
        return object;
    }

    public static void teardown(final ContextConfig contextConfig) {
        final Context context = AppNaming.lookupSubcontext(contextConfig.getName());
        for (ContextObject objectConfig : contextConfig.getObjects()) {
            final Object object = AppNaming.lookup(context, objectConfig.getName());
            AppNaming.unbind(context, objectConfig.getName());
            if (object instanceof Disposable) {
                ((Disposable) object).dispose();
            }
        }
        AppNaming.destroySubcontext(contextConfig.getName());
    }
}
