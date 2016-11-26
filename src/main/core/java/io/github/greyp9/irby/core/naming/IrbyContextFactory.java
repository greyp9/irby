package io.github.greyp9.irby.core.naming;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;

public class IrbyContextFactory implements javax.naming.spi.InitialContextFactory {
    private static final IrbyContext CONTEXT = new IrbyContext(null);

    @SuppressWarnings("PMD.ReplaceHashtableWithMap")
    @Override
    public final Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {
        //java.util.logging.Logger.getLogger(getClass().getName()).info(
        //        "getInitialContext()::" + environment.toString() + "::" + CONTEXT);
        return CONTEXT;
    }
}
