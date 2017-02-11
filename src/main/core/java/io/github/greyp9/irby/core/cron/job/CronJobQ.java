package io.github.greyp9.irby.core.cron.job;

import java.util.Date;

public class CronJobQ {
    private final String name;
    private final Date date;

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public CronJobQ(String name, Date date) {
        this.name = name;
        this.date = date;
    }
}
