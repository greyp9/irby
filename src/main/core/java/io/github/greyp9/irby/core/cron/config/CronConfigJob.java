package io.github.greyp9.irby.core.cron.config;

import org.w3c.dom.Element;

public class CronConfigJob {
    private final String name;
    private final String schedule;
    private final String className;
    private final Element element;

    public final String getName() {
        return name;
    }

    public final String getSchedule() {
        return schedule;
    }

    public final String getClassName() {
        return className;
    }

    public final Element getElement() {
        return element;
    }

    public CronConfigJob(final String name, final String schedule, final String className, final Element element) {
        this.name = name;
        this.schedule = schedule;
        this.className = className;
        this.element = element;
    }
}
