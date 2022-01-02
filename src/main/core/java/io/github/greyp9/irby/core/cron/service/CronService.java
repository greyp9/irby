package io.github.greyp9.irby.core.cron.service;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.arwo.core.vm.mutex.CollectionU;
import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.cron.factory.JobFactory;
import io.github.greyp9.irby.core.cron.impl.CommandRunnable;
import io.github.greyp9.irby.core.cron.job.CronJobQ;
import io.github.greyp9.irby.core.cron.job.CronJobX;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final Collection<CronJobX> jobsX;
    private final Collection<CronJobQ> jobsQ;

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
        this.jobsX = new ArrayList<>();
        this.jobsQ = new ArrayList<>();
        for (final CronConfigJob job : config.getJobs()) {
            jobsX.add(CronJobX.create(config, job));
        }
        final String prefix = String.format("%s-%s", getClass().getSimpleName(), config.getName());
        this.executorService = (config.isLocalExecutor()
                ? ExecutorServiceFactory.create(config.getThreads(), prefix) : executorService);
        this.reference = reference;
    }

    public final void run(final CronJobQ... cronJobQ) {
        //Arrays.stream(cronJobQ).map(CronJobQ::toString).forEach(logger::info);
        Arrays.stream(cronJobQ).forEach(cronJobQ1 -> CollectionU.add(jobsQ, cronJobQ1));
        MutexU.notifyAll(this);
    }

    public final void run() {
        Date dateNext = new Date();
        while (reference.get() == null) {
            dateNext = getNextTime(dateNext, Const.DURATION_WORKLOOP);
            logger.log(Level.FINEST, "run()::dateNext=" + XsdDateU.toXSDZMillis(dateNext));
            MutexU.waitUntil(this, dateNext);
            final Collection<CronJobQ> jobs = CollectionU.move(new ArrayList<>(), jobsQ);
            if (reference.get() == null) {
                if (jobs.isEmpty()) {
                    doJobsScheduled(dateNext);
                } else {
                    doJobsRequested(jobs);
                    dateNext = new Date();
                }
            }
        }
    }

    private Date getNextTime(final Date date, final String duration) {
        Date dateNext = DurationU.add(date, timeZone, duration);
        //logger.log(Level.FINEST, "getNextTime()::dateNext=" + XsdDateU.toXSDZMillis(dateNext));
        for (final CronJobX job : jobsX) {
            final Date dateNextJob = job.getJob().getDateNext(date, timeZone, duration);
            final boolean isEarlier = ((dateNextJob != null) && (dateNextJob.before(dateNext)));
            dateNext = (isEarlier ? dateNextJob : dateNext);
        }
        //logger.log(Level.FINEST, "getNextTime()::dateNext=" + XsdDateU.toXSDZMillis(dateNext));
        return dateNext;
    }

    private void doJobsRequested(final Collection<CronJobQ> jobs) {
        logger.finest(String.format("REQUESTED:[%s]", jobs));
        for (final CronJobQ jobQ : jobs) {
            for (final CronJobX jobX : jobsX) {
                if (jobX.getName().equals(jobQ.getName())) {
                    doJob(jobQ.getTab(), jobQ.getName(), jobQ.getDate(), jobX);
                }
            }
        }
    }

    private void doJobsScheduled(final Date date) {
        logger.finest(String.format("SCHEDULED:[%s]", XsdDateU.toXSDZMillis(date)));
        for (final CronJobX job : jobsX) {
            if (job.getJob().isReady(date, timeZone)) {
                doJob(job.getTab(), job.getJob().getName(), date, job);
            }
        }
    }

    private void doJob(final String tab, final String jobName, final Date date, final CronJobX job) {
        final JobFactory factory = new JobFactory();
        final String className = lookupClassName(job.getClassName());
        final Runnable runnable = factory.getRunnable(className, job.getElement(), tab, jobName, date);
        if (runnable instanceof CommandRunnable) {
            ((CommandRunnable) runnable).setExecutorService(executorService);
        }
        if (runnable != null) {
            executorService.execute(runnable);
        }
    }

    private static String lookupClassName(final String type) {
        String className = null;
        if ("arguments".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.ArgumentsRunnable";
        } else if ("sleep".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.SleepRunnable";
        } else if ("command".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.CommandRunnable";
        } else if ("generic".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.GenericRunnable";
        } else if ("batch".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.BatchRunnable";
        } else if ("copy-file".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.file.CopyFileRunnable";
        } else if ("compress-file".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.file.CompressFileRunnable";
        } else if ("group-file".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.file.GroupFileRunnable";
        } else if ("regroup-file".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.file.RegroupFileRunnable";
        } else if ("http".equals(type)) {
            className = "io.github.greyp9.irby.core.cron.impl.net.HttpRunnable";
        }
        return className;
    }

    private static class Const {
        private static final String DURATION_WORKLOOP = DurationU.Const.ONE_MINUTE;
    }
}
