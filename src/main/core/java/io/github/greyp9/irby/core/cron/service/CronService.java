package io.github.greyp9.irby.core.cron.service;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.cron.factory.JobFactory;
import io.github.greyp9.irby.core.cron.job.CronJobX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class CronService {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final CronConfig config;
    private final TimeZone timeZone;
    private final Collection<CronJobX> jobs;

    private final ExecutorService executorService;
    private final AtomicReference<String> reference;

    public final CronConfig getConfig() {
        return config;
    }

    public CronService(final CronConfig config,
                       final ExecutorService executorService,
                       final AtomicReference<String> reference) {
        this.config = config;
        this.timeZone = TimeZone.getTimeZone(config.getTimezone());
        this.jobs = new ArrayList<CronJobX>();
        for (final CronConfigJob job : config.getJobs()) {
            jobs.add(CronJobX.create(job));
        }
        final String prefix = String.format("%s-%s", getClass().getSimpleName(), config.getName());
        this.executorService = (config.isLocalExecutor() ?
                ExecutorServiceFactory.create(config.getThreads(), prefix) : executorService);
        this.reference = reference;
    }

    public final void run() {
        Date dateNext = new Date();
        while (reference.get() == null) {
            dateNext = getNextTime(dateNext, Const.DURATION_WORKLOOP);
            logger.log(Level.FINEST, XsdDateU.toXSDZMillis(dateNext));
            MutexU.waitUntil(this, dateNext);
            if (reference.get() == null) {
                doJobs(dateNext);
            }
        }
    }

    private Date getNextTime(final Date date, final String duration) {
        Date dateNext = DurationU.add(date, timeZone, duration);
        for (final CronJobX job : jobs) {
            final Date dateNextJob = job.getJob().getDateNext(date, timeZone, duration);
            final boolean isEarlier = ((dateNextJob != null) && (dateNextJob.before(dateNext)));
            dateNext = (isEarlier ? dateNextJob : dateNext);
        }
        return dateNext;
    }

    private void doJobs(final Date date) {
        for (final CronJobX job : jobs) {
            if (job.getJob().isReady(date, timeZone)) {
                doJob(date, job);
            }
        }
    }

    private void doJob(final Date date, final CronJobX job) {
        final JobFactory factory = new JobFactory();
        final Runnable runnable = factory.getRunnable(job.getClassName(), job.getElement(), date);
        if (runnable != null) {
            executorService.execute(runnable);
        }
    }

    private static class Const {
        private static final String DURATION_WORKLOOP = DurationU.Const.ONE_MINUTE;
    }
}
