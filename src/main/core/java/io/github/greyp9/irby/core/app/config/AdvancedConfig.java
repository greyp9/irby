package io.github.greyp9.irby.core.app.config;

import java.util.Collection;
import java.util.Properties;

public final class AdvancedConfig {
    private final String name;
    private final Properties properties;

    public AdvancedConfig(final String name, final Properties properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public Properties getProperties() {
        return properties;
    }

    public Collection<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }
}
