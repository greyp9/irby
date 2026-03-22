package io.github.greyp9.irby.core.cl.config;

import java.util.ArrayList;
import java.util.Collection;

public final class ClassLoaderConfig {
    private final String name;
    private final Collection<ResourceConfig> resources;

    public String getName() {
        return name;
    }

    public Collection<ResourceConfig> getResources() {
        return resources;
    }

    public ClassLoaderConfig(final String name) {
        this.name = name;
        this.resources = new ArrayList<>();
    }
}
