package io.github.greyp9.irby.core.cron.service;

import io.github.greyp9.arwo.core.date.DateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.io.command.CommandWork;
import io.github.greyp9.arwo.core.table.filter.Filters;
import io.github.greyp9.arwo.core.table.metadata.ColumnMetaData;
import io.github.greyp9.arwo.core.table.metadata.RowSetMetaData;
import io.github.greyp9.arwo.core.table.model.Table;
import io.github.greyp9.arwo.core.table.row.RowSet;
import io.github.greyp9.arwo.core.table.sort.Sorts;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.arwo.core.vm.mutex.CollectionU;
import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.cron.factory.JobFactory;
import io.github.greyp9.irby.core.cron.impl.CommandRunnable;
import io.github.greyp9.irby.core.cron.job.CronJobQ;
import io.github.greyp9.irby.core.cron.job.CronJobX;
import io.github.greyp9.irby.core.cron.widget.ExecutorAdaptor;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
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

    private final Collection<CommandWork> commands;
    private final ExecutorService executorService;
    private final ExecutorService executorServiceCmd;

    private final AtomicReference<Date> dateStandbyUntil;
    private final AtomicReference<String> reference;

    public final CronConfig getConfig() {
        return config;
    }

    public final Collection<CommandWork> getCommands() {
        return CollectionU.copy(new ArrayList<>(), commands);
    }

    public final Table getQueue() {
        final RowSetMetaData metaData = new RowSetMetaData("cronQueueType",
                new ColumnMetaData("name", Types.VARCHAR),
                new ColumnMetaData("type", Types.VARCHAR),
                new ColumnMetaData("date", Types.TIMESTAMP));
        final RowSet rowSet = new RowSet(metaData, null, null);
        if (executorService instanceof ThreadPoolExecutor) {
            ExecutorAdaptor.addContentTo(rowSet, (ThreadPoolExecutor) executorService);
        }
        return new Table(rowSet, new Sorts(), new Filters(), null, null);
    }

    public final Date getDateStandby() {
        return dateStandbyUntil.get();
    }

    public final void setStandby(final String duration) {
        dateStandbyUntil.set(DurationU.add(new Date(), timeZone, duration));
        logger.info(XsdDateU.toXSDZMillis(dateStandbyUntil.get()));
    }

    public CronService(final CronConfig config,
                       final ExecutorService executorService,
                       final AtomicReference<String> reference) {
        this.config = config;
        this.timeZone = TimeZone.getTimeZone(config.getTimezone());
        this.jobsX = new ArrayList<>();
        this.jobsQ = new ArrayList<>();
        this.commands = new ArrayList<>();
        for (final CronConfigJob job : config.getJobs()) {
            jobsX.add(CronJobX.create(config, job));
        }
        final String prefix = String.join("-", getClass().getSimpleName(), config.getName());
        final String prefixCmd = String.join("-", prefix, "cmd");
        this.executorService = ((config.getThreadsJob() > 0)
                ? ExecutorServiceFactory.create(config.getThreadsJob(), prefix) : executorService);
        this.executorServiceCmd = ((config.getThreadsStream() > 0)
                ? ExecutorServiceFactory.create(config.getThreadsStream(), prefixCmd) : executorService);
        this.dateStandbyUntil = new AtomicReference<>(new Date());
        this.reference = reference;
    }

    public final void run(final CronJobQ... cronJobQ) {
        //Arrays.stream(cronJobQ).map(CronJobQ::toString).forEach(logger::info);
        Arrays.stream(cronJobQ).forEach(cronJobQ1 -> CollectionU.add(jobsQ, cronJobQ1));
        MutexU.notifyAll(this);
    }

    public final void run() {
        Date dateNextScheduled = DateU.ceiling(new Date(), Const.DURATION_WORKLOOP);
        while (reference.get() == null) {
            logger.log(Level.FINEST, "run()::dateNext=" + XsdDateU.toXSDZMillis(dateNextScheduled));
            // requested jobs
            doJobsRequested(CollectionU.move(new ArrayList<>(), jobsQ));
            MutexU.waitUntil(this, dateNextScheduled);
            // scheduled jobs
            if (reference.get() == null) {
                final Date date = new Date();
                if (date.compareTo(dateNextScheduled) >= 0) {
                    if (date.compareTo(dateStandbyUntil.get()) >= 0) {
                        doJobsScheduled(dateNextScheduled);
                    }
                    dateNextScheduled = getNextTime(dateNextScheduled);
                }
            }
        }
        executorServiceCmd.shutdown();
    }

    private Date getNextTime(final Date date) {
        final String duration = Const.DURATION_WORKLOOP;
        final Date dateIterate = DurationU.add(date, timeZone, duration);
        Date dateNext = dateIterate;
        logger.log(Level.FINEST, "getNextTime()::beginCheckSchedules=" + XsdDateU.toXSDZMillis(dateNext));
        for (final CronJobX job : jobsX) {
            final Date dateNextJob = job.getJob().getDateNext(date, timeZone, duration);
            final boolean isEarlier = ((dateNextJob != null) && (dateNextJob.before(dateNext)));
            dateNext = (isEarlier ? dateNextJob : dateNext);
        }
        logger.log(Level.FINEST, "getNextTime()::endCheckSchedules=" + XsdDateU.toXSDZMillis(dateNext));
        return dateNext;
    }

    private void doJobsRequested(final Collection<CronJobQ> jobs) {
        if (!jobs.isEmpty()) {
            logger.finest(String.format("REQUESTED:[%d]", jobs.size()));
            for (final CronJobQ jobQ : jobs) {
                for (final CronJobX jobX : jobsX) {
                    if (jobX.getName().equals(jobQ.getName())) {
                        doJob(jobQ.getTab(), jobQ.getName(), jobQ.getDate(), jobX);
                    }
                }
            }
            //logger.finest(config.getName() + ":" + ThreadPoolU.getTelemetry(this.executorService));
        }
    }

    private void doJobsScheduled(final Date date) {
        logger.finest(String.format("SCHEDULED:[%s]", XsdDateU.toXSDZMillis(date)));
        for (final CronJobX job : jobsX) {
            if (job.getJob().isReady(date, timeZone)) {
                doJob(job.getTab(), job.getJob().getName(), date, job);
            }
        }
        //logger.finest(config.getName() + ":" + ThreadPoolU.getTelemetry(this.executorService));
    }

    private void doJob(final String tab, final String jobName, final Date date, final CronJobX job) {
        final JobFactory factory = new JobFactory();
        final String className = lookupClassName(job.getClassName());
        final Runnable runnable = factory.getRunnable(className, job.getElement(), tab, jobName, date);
        if (runnable instanceof CommandRunnable) {
            ((CommandRunnable) runnable).setExecutorService(executorService);
            ((CommandRunnable) runnable).setExecutorServiceCmd(executorServiceCmd);
            ((CommandRunnable) runnable).setCommands(commands);
        }
        if (runnable != null) {
            // ExecutorService.submit() queues a FutureTask, with no access to interesting data
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
