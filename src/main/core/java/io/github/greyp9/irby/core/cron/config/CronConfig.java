package io.github.greyp9.irby.core.cron.config;

import java.util.ArrayList;
import java.util.Collection;

public class CronConfig {
    private final String name;
    private final String timezone;
    private final int threadsJob;
    private final int threadsStream;
    private final String service;
    private final Collection<CronConfigJob> jobs;

    public final String getName() {
        return name;
    }

    public final String getTimezone() {
        return timezone;
    }

    public final int getThreadsJob() {
        return threadsJob;
    }

    public final int getThreadsStream() {
        return threadsStream;
    }

    public final String getService() {
        return service;
    }

    public final Collection<CronConfigJob> getJobs() {
        return jobs;
    }

    public CronConfig(final String name, final String timezone, final String service,
                      final int threadsJob, final int threadsStream) {
        this.name = name;
        this.timezone = timezone;
        this.service = service;
        this.threadsJob = threadsJob;
        this.threadsStream = threadsStream;
        this.jobs = new ArrayList<CronConfigJob>();
    }

    public final void addJob(final CronConfigJob job) {
        jobs.add(job);
    }
}
