package io.github.greyp9.irby.core.context.config;

import java.util.ArrayList;
import java.util.Collection;

public final class ContextConfig {
    private final String name;
    private final Collection<ContextObject> objects;

    public String getName() {
        return name;
    }

    public Collection<ContextObject> getObjects() {
        return objects;
    }

    public ContextObject getObjectByName(final String name) {
        return objects.stream().filter(o -> o.getName().equals(name)).findFirst().orElse(null);
    }

    public ContextConfig(final String name) {
        this.name = name;
        this.objects = new ArrayList<ContextObject>();
    }

    public void addObject(final ContextObject object) {
        objects.add(object);
    }
}
