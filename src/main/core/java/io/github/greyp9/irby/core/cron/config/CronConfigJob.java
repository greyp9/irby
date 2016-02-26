package io.github.greyp9.irby.core.cron.config;

public class CronConfigJob {
    private final String name;
    private final String line;

    public final String getName() {
        return name;
    }

    public final String getLine() {
        return line;
    }

    public CronConfigJob(final String name, final String line) {
        this.name = name;
        this.line = line;
    }
}
