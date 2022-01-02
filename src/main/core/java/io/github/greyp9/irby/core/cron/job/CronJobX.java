package io.github.greyp9.irby.core.cron.job;

import io.github.greyp9.arwo.core.cron.job.CronJob;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import org.w3c.dom.Element;

public final class CronJobX {
    private final CronConfig cronConfig;
    private final CronConfigJob cronConfigJob;
    private final CronJob job;

    public String getTab() {
        return cronConfig.getName();
    }

    public String getName() {
        return cronConfigJob.getName();
    }

    public String getSchedule() {
        return cronConfigJob.getSchedule();
    }

    public String getClassName() {
        return cronConfigJob.getClassName();
    }

    public Element getElement() {
        return cronConfigJob.getElement();
    }

    public CronJob getJob() {
        return job;
    }

    private CronJobX(final CronConfig cronConfig, final CronConfigJob cronConfigJob) {
        this.cronConfig = cronConfig;
        this.cronConfigJob = cronConfigJob;
        final String line = String.format("%s %s", cronConfigJob.getSchedule(), cronConfigJob.getClassName());
        this.job = new CronJob(cronConfigJob.getName(), true, line, cronConfigJob.getElement());
    }

    public static CronJobX create(final CronConfig config, final CronConfigJob configJob) {
        return new CronJobX(config, configJob);
    }
}
