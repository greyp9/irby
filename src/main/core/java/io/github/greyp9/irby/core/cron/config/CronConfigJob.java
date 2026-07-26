package io.github.greyp9.irby.core.cron.config;

import org.w3c.dom.Element;

import java.util.List;

public class CronConfigJob {
    private final String name;
    private final String schedule;
    private final String className;
    private final Element element;
    private final List<Element> elements;

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

    public final List<Element> getElements() {
        return elements;
    }

    public CronConfigJob(final String name, final String schedule, final String className,
                         final Element element, final List<Element> elements) {
        this.name = name;
        this.schedule = schedule;
        this.className = className;
        this.element = element;
        this.elements = elements;
    }
}
