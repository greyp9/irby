package io.github.greyp9.irby.core.http11.config;

import java.util.Properties;

public class Http11ConfigServlet {
    private final String name;
    private final String path;
    private final String className;
    private final String authConstraint;
    private final Properties initParams;

    public final String getName() {
        return name;
    }

    public final String getPath() {
        return path;
    }

    public final String getClassName() {
        return className;
    }

    public final String getAuthConstraint() {
        return authConstraint;
    }

    public final String getInitParam(final String key) {
        return initParams.getProperty(key);
    }

    public final Properties getInitParams() {
        return initParams;
    }

    public Http11ConfigServlet(
            final String name, final String path, final String className, final String authConstraint) {
        this.name = name;
        this.path = path;
        this.className = className;
        this.authConstraint = authConstraint;
        this.initParams = new Properties();
    }

    public final void addInitParam(final String initParamName, final String initParamValue) {
        initParams.setProperty(initParamName, initParamValue);
    }
}
