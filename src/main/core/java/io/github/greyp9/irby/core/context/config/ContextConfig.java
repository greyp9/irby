package io.github.greyp9.irby.core.context.config;

import java.util.ArrayList;
import java.util.Collection;

public class ContextConfig {
    private final String name;
    private final Collection<ContextObject> objects;

    public String getName() {
        return name;
    }

    public Collection<ContextObject> getObjects() {
        return objects;
    }

    public ContextConfig(String name) {
        this.name = name;
        this.objects = new ArrayList<ContextObject>();
    }

    public final void addObject(final ContextObject object) {
        objects.add(object);
    }
}
