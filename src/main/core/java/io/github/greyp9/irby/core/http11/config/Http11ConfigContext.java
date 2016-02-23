package io.github.greyp9.irby.core.http11.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class Http11ConfigContext {
    private final String name;
    private final String path;
    private final String realm;
    private final Properties contextParams;
    private final Collection<Http11ConfigServlet> servlets;

    public final String getName() {
        return name;
    }

    public final String getPath() {
        return path;
    }

    public final String getRealm() {
        return realm;
    }

    public final String getContextParam(final String key) {
        return contextParams.getProperty(key);
    }

    public final Properties getContextParams() {
        return contextParams;
    }

    public final Collection<Http11ConfigServlet> getServlets() {
        return servlets;
    }

    public Http11ConfigContext(final String name, final String path, final String realm) {
        this.name = name;
        this.path = path;
        this.realm = realm;
        this.contextParams = new Properties();
        this.servlets = new ArrayList<Http11ConfigServlet>();
    }

    public final void addContextParam(final String contextParamName, final String contextParamValue) {
        contextParams.setProperty(contextParamName, contextParamValue);
    }

    public final void addServlet(final Http11ConfigServlet servlet) {
        servlets.add(servlet);
    }
}
