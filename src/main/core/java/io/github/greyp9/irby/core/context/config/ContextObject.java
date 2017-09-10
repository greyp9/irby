package io.github.greyp9.irby.core.context.config;

import java.util.ArrayList;
import java.util.Collection;

public class ContextObject {
    private final String name;
    private final String type;
    private final Collection<String> parameters;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Collection<String> getParameters() {
        return parameters;
    }

    public ContextObject(String name, String type) {
        this.name = name;
        this.type = type;
        this.parameters = new ArrayList<String>();
    }

    public final void addParameter(final String parameter) {
        parameters.add(parameter);
    }
}
