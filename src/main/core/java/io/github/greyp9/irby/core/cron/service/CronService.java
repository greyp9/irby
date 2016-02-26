package io.github.greyp9.irby.core.cron.service;

import io.github.greyp9.arwo.core.cron.job.CronJob;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CronService {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final CronConfig config;
    private final TimeZone timeZone;
    private final Collection<CronJob> jobs;

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
        this.jobs = new ArrayList<CronJob>();
        for (final CronConfigJob job : config.getJobs()) {
            jobs.add(create(job));
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

    private CronJob create(final CronConfigJob job) {
        return new CronJob(job.getName(), true, job.getLine(), null);
    }

    private Date getNextTime(final Date date, final String duration) {
        Date dateNext = DurationU.add(date, timeZone, duration);
        for (final CronJob job : jobs) {
            final Date dateNextJob = job.getDateNext(date, timeZone, duration);
            final boolean isEarlier = ((dateNextJob != null) && (dateNextJob.before(dateNext)));
            dateNext = (isEarlier ? dateNextJob : dateNext);
        }
        return dateNext;
    }

    private void doJobs(final Date date) {
        executorService.getClass();
        for (final CronJob job : jobs) {
            if (job.isReady(date, timeZone)) {
                logger.log(Level.FINEST, String.format("[%s][%s][%s]", "", job.getName(), job.getLine()));
            }
        }
    }

    private static class Const {
        private static final String DURATION_WORKLOOP = DurationU.Const.ONE_MINUTE;
    }
}
