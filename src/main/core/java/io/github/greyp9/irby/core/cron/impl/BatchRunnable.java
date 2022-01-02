package io.github.greyp9.irby.core.cron.impl;

import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.xml.ElementU;
import io.github.greyp9.irby.core.cron.job.CronJobQ;
import io.github.greyp9.irby.core.cron.service.CronService;
import org.w3c.dom.Element;

import javax.naming.Context;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"PMD.DoNotUseThreads", "unused"})
public class BatchRunnable extends CronRunnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public BatchRunnable(final String tab, final String name, final Date date, final Element element) {
        super(tab, name, date, element);
    }

    @Override
    public final void run() {
        final String className = getClass().getName();
        final String date = XsdDateU.toXSDZMillis(getDate());
        final String methodName = String.format("run(%s)", date);
        logger.entering(className, methodName);
        final String jobs = ElementU.getAttribute(getElement(), Const.JOBS);
        logger.log(Level.FINE, String.format("[%s][%s][%s][%s]",
                XsdDateU.toXSDZMillis(getDate()), getTab(), getName(), jobs));
        final String[] jobNames = jobs.split("\\s+");
        final Context context = AppNaming.lookupSubcontext(CronService.class.getName());
        final Object o = AppNaming.lookup(context, getTab());
        if (o instanceof CronService) {
            final CronService cronService = (CronService) o;
            cronService.run(Arrays.stream(jobNames)
                    .map(job -> new CronJobQ(getTab(), job, getDate()))
                    .toArray(CronJobQ[]::new));
        }
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String JOBS = "jobs";  // i18n internal
    }
}
