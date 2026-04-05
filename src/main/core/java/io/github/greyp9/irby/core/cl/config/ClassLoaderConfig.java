package io.github.greyp9.irby.core.cl.config;

public final class ClassLoaderConfig {
    private final String name;
    private final String resources;

    public String getName() {
        return name;
    }

    public String getResources() {
        return resources;
    }

    public ClassLoaderConfig(final String name, final String resources) {
        this.name = name;
        this.resources = resources;
    }
}
