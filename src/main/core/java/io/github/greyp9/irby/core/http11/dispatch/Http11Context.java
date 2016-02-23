package io.github.greyp9.irby.core.http11.dispatch;

import io.github.greyp9.irby.core.http11.config.Http11ConfigContext;
import io.github.greyp9.irby.core.http11.config.Http11ConfigServlet;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletContext;
import io.github.greyp9.irby.core.realm.Realm;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Http11Context {
    private final Http11ConfigContext config;
    private final Realm realm;
    private final Map<String, Http11Servlet> servlets;

    public final Http11ConfigContext getConfig() {
        return config;
    }

    public final Realm getRealm() {
        return realm;
    }

    public Http11Context(final Http11ConfigContext config, final Realm realm) {
        this.config = config;
        this.realm = realm;
        this.servlets = new TreeMap<String, Http11Servlet>();  // servlet select algorithm needs sorted servlet map
    }

    public final void unregister() {
        synchronized (servlets) {
            for (final Http11Servlet servlet : servlets.values()) {
                servlet.getHttpServlet().destroy();
            }
            servlets.clear();
        }
    }

    public final void register(
            final Collection<Http11ConfigServlet> servletConfigs, final Http11ServletContext servletContext) {
        final Http11ServletFactory factory = new Http11ServletFactory();
        synchronized (servlets) {
            for (final Http11ConfigServlet servletConfig : servletConfigs) {
                final Http11Servlet servlet = factory.create(servletConfig, servletContext, realm);
                if (servlet != null) {
                    servlets.put(servletConfig.getPath(), servlet);
                }
            }
        }
    }

    public final Http11Servlet select(final String uri) {
        Http11Servlet servlet = null;
        final String uriServlet = uri.substring(config.getPath().length());
        synchronized (servlets) {
            for (final Map.Entry<String, Http11Servlet> entry : servlets.entrySet()) {
                final String servletPath = entry.getKey();
                if (uriServlet.startsWith(servletPath)) {
                    servlet = entry.getValue();
                }
            }
        }
        return servlet;
    }
}
