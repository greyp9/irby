package io.github.greyp9.irby.core.cron.impl;

import org.w3c.dom.Element;

import java.util.Date;

@SuppressWarnings({ "PMD.AbstractNaming", "PMD.DoNotUseThreads" })
public abstract class CronRunnable implements Runnable {
    private final String tab;
    private final String name;
    private final Date date;
    private final Element element;

    public final String getTab() {
        return tab;
    }

    public final String getName() {
        return name;
    }

    public final Date getDate() {
        return date;
    }

    public final Element getElement() {
        return element;
    }

    protected CronRunnable(final String tab, final String name, final Date date, final Element element) {
        this.tab = tab;
        this.name = name;
        this.date = date;
        this.element = element;
    }
}
