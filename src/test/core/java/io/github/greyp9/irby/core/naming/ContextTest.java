package io.github.greyp9.irby.core.naming;

import io.github.greyp9.arwo.core.naming.AppNaming;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.Context;
import java.util.logging.Logger;

public class ContextTest {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @BeforeEach
    public final void setUp() throws Exception {
        io.github.greyp9.arwo.core.logging.LoggerU.adjust(Logger.getLogger(""));
    }

    @Test
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

    @Test
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
        Assertions.assertSame(context1, context1Read, "expect identity equals");
        Assertions.assertSame(context2, context2Read, "expect identity equals");

        Assertions.assertEquals(context1.getClass(), IrbyContext.class, "expect custom type");
        Assertions.assertEquals(context2.getClass(), IrbyContext.class, "expect custom type");

        final IrbyContext irbyContext1 = (IrbyContext) context1Read;
        final IrbyContext irbyContext2 = (IrbyContext) context2Read;
        Assertions.assertSame(
                irbyContext1.getParentContext(), irbyContext2.getParentContext(), "root context parent of both");

        final IrbyContext rootContext = irbyContext1.getParentContext();
        Assertions.assertNull(rootContext.getParentContext(), "root context has no parent");
    }

    @Test
    public void testBinding() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, IrbyContextFactory.class.getName());
        final Context context1 = AppNaming.createSubcontext("/test1");
        logger.info(context1.toString());
        String s1Name = "bindingName1";
        String s1Value = "bindingValue1";
        AppNaming.bind(context1, s1Name, s1Value);
        String s1ValueRead = (String) AppNaming.lookup("/test1", s1Name);
        Assertions.assertSame(s1Value, s1ValueRead, "CRUD identity");
    }
}
