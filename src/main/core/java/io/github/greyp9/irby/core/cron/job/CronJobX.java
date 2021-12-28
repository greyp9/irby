package io.github.greyp9.irby.core.cron.job;

import io.github.greyp9.arwo.core.cron.job.CronJob;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import org.w3c.dom.Element;

public final class CronJobX {
    private final CronConfigJob config;
    private final CronJob job;

    public String getName() {
        return config.getName();
    }

    public String getSchedule() {
        return config.getSchedule();
    }

    public String getClassName() {
        return config.getClassName();
    }

    public Element getElement() {
        return config.getElement();
    }

    public CronJob getJob() {
        return job;
    }

    private CronJobX(final CronConfigJob config) {
        this.config = config;
        final String line = String.format("%s %s", config.getSchedule(), config.getClassName());
        this.job = new CronJob(config.getName(), true, line, config.getElement());
    }

    public static CronJobX create(final CronConfigJob config) {
        return new CronJobX(config);
    }
}
