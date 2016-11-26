package io.github.greyp9.irby.core.naming;

public class Binding {
    private final String name;
    private final Object value;

    public Binding(final String name, final Object value) {
        this.name = name;
        this.value = value;
    }

    public final String getName() {
        return name;
    }

    public final Object getValue() {
        return value;
    }

    public final String toString() {
        return name + "::" + value;
    }
}
