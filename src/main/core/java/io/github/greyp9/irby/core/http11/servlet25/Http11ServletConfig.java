package io.github.greyp9.irby.core.http11.servlet25;

import io.github.greyp9.irby.core.http11.config.Http11ConfigServlet;

import javax.servlet.ServletContext;
import java.util.Enumeration;

public class Http11ServletConfig implements javax.servlet.ServletConfig {
    private final Http11ConfigServlet config;
    private final Http11ServletContext servletContext;

    public final Http11ConfigServlet getConfig() {
        return config;
    }

    public Http11ServletConfig(final Http11ConfigServlet config, final Http11ServletContext servletContext) {
        this.config = config;
        this.servletContext = servletContext;
    }

    @Override
    public final String getServletName() {
        return config.getName();
    }

    @Override
    public final ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public final String getInitParameter(final String s) {
        return config.getInitParam(s);
    }

    @Override
    public final Enumeration getInitParameterNames() {
        return config.getInitParams().propertyNames();
    }
}
