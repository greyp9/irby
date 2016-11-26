package io.github.greyp9.irby.core.naming;

import io.github.greyp9.arwo.core.naming.AppNaming;
import junit.framework.TestCase;
import org.junit.Assert;

import javax.naming.Context;
import java.util.logging.Logger;

public class ContextTest extends TestCase {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void setUp() throws Exception {
        super.setUp();
        io.github.greyp9.arwo.core.logging.LoggerU.adjust(Logger.getLogger(""));
    }

    public void testMultipleCreates() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, IrbyContextFactory.class.getName());
        final Context context1a = AppNaming.createSubcontext("/test1");
        logger.info(context1a.toString());
        final Context context1aRead = AppNaming.lookupSubcontext("/test1");
        logger.info(context1aRead.toString());
        final Context context1b = AppNaming.createSubcontext("/test1");
        logger.info(context1b.toString());
        final Context context1bRead = AppNaming.lookupSubcontext("/test1");
        logger.info(context1bRead.toString());
        final Context context1c = AppNaming.createSubcontext("/test1");
        logger.info(context1c.toString());
    }

    public void testMultipleSubcontexts() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, IrbyContextFactory.class.getName());
        final Context context1 = AppNaming.createSubcontext("/test1");
        logger.info(context1.toString());
        final Context context2 = AppNaming.createSubcontext("/test2");
        logger.info(context2.toString());
        final Context context1Read = AppNaming.lookupSubcontext("/test1");
        logger.info(context1Read.toString());
        final Context context2Read = AppNaming.lookupSubcontext("/test2");
        logger.info(context2Read.toString());
        Assert.assertSame("expect identity equals", context1, context1Read);
        Assert.assertSame("expect identity equals", context2, context2Read);

        Assert.assertEquals("expect custom type", context1.getClass(), IrbyContext.class);
        Assert.assertEquals("expect custom type", context2.getClass(), IrbyContext.class);

        final IrbyContext irbyContext1 = (IrbyContext) context1Read;
        final IrbyContext irbyContext2 = (IrbyContext) context2Read;
        Assert.assertSame("root context parent of both",
                irbyContext1.getParentContext(), irbyContext2.getParentContext());

        final IrbyContext rootContext = irbyContext1.getParentContext();
        Assert.assertNull("root context has no parent", rootContext.getParentContext());
    }

    public void testBinding() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, IrbyContextFactory.class.getName());
        final Context context1 = AppNaming.createSubcontext("/test1");
        logger.info(context1.toString());
        String s1Name = "bindingName1";
        String s1Value = "bindingValue1";
        AppNaming.bind(context1, s1Name, s1Value);
        String s1ValueRead = (String) AppNaming.lookup("/test1", s1Name);
        Assert.assertSame("CRUD identity", s1Value, s1ValueRead);
    }
}
