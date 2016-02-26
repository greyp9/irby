package io.github.greyp9.irby.core.cron.config;

import java.util.ArrayList;
import java.util.Collection;

public class CronConfig {
    private final String name;
    private final String timezone;
    private final int threads;
    private final Collection<CronConfigJob> jobs;

    public final String getName() {
        return name;
    }

    public final String getTimezone() {
        return timezone;
    }

    public final int getThreads() {
        return threads;
    }

    public final Collection<CronConfigJob> getJobs() {
        return jobs;
    }

    public CronConfig(final String name, final String timezone, final int threads) {
        this.name = name;
        this.timezone = timezone;
        this.threads = threads;
        this.jobs = new ArrayList<CronConfigJob>();
    }

    public final boolean isLocalExecutor() {
        return (threads > 0);
    }

    public final void addJob(final CronConfigJob job) {
        jobs.add(job);
    }
}
