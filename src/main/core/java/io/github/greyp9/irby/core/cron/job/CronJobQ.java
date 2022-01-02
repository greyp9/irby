package io.github.greyp9.irby.core.cron.job;

import java.util.Date;

public final class CronJobQ {
    private final String tab;
    private final String name;
    private final Date date;

    public String getTab() {
        return tab;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public CronJobQ(final String tab, final String name, final Date date) {
        this.tab = tab;
        this.name = name;
        this.date = date;
    }
}
