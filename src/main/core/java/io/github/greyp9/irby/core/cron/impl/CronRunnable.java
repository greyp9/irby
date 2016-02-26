package io.github.greyp9.irby.core.cron.impl;

import org.w3c.dom.Element;

import java.util.Date;

@SuppressWarnings({ "PMD.AbstractNaming", "PMD.DoNotUseThreads" })
public abstract class CronRunnable implements Runnable {
    private final Date date;
    private final Element element;

    public final Date getDate() {
        return date;
    }

    public final Element getElement() {
        return element;
    }

    protected CronRunnable(final Date date, final Element element) {
        this.date = date;
        this.element = element;
    }
}
