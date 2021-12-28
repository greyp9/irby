package io.github.greyp9.irby.core.cron.job;

import java.util.Date;

public final class CronJobQ {
    private final String name;
    private final Date date;

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public CronJobQ(final String name, final Date date) {
        this.name = name;
        this.date = date;
    }
}
