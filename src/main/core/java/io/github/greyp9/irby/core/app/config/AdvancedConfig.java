package io.github.greyp9.irby.core.app.config;

import java.util.Collection;
import java.util.Properties;

public class AdvancedConfig {
    final String name;
    final Properties properties;

    public AdvancedConfig(final String name, final Properties properties) {
        this.name = name;
        this.properties = properties;
    }

    public final String getName() {
        return name;
    }

    public final Properties getProperties() {
        return properties;
    }

    public final Collection<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }
}
