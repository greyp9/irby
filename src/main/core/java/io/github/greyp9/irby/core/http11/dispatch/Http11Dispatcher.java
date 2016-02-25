package io.github.greyp9.irby.core.http11.dispatch;

import io.github.greyp9.irby.core.http11.config.Http11Config;
import io.github.greyp9.irby.core.http11.config.Http11ConfigContext;
import io.github.greyp9.irby.core.http11.payload.Http11Request;
import io.github.greyp9.irby.core.http11.payload.Http11Response;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletContext;
import io.github.greyp9.irby.core.realm.Realm;
import io.github.greyp9.irby.core.realm.Realms;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Http11Dispatcher {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Http11Config config;
    private final Realms realms;
    private final Map<String, Http11Context> contexts;

    public final Http11Config getConfig() {
        return config;
    }

    public Http11Dispatcher(final Http11Config config, final Realms realms) {
        this.config = config;
        this.realms = realms;
        this.contexts = new TreeMap<String, Http11Context>();
    }

    public final void unregister() {
        synchronized (contexts) {
            for (final Http11Context http11Context : contexts.values()) {
                http11Context.unregister();
            }
            contexts.clear();
        }
    }

    public final void register(final Collection<Http11ConfigContext> contextConfigs) {
        synchronized (contexts) {
            for (final Http11ConfigContext contextConfig : contextConfigs) {
                register(contextConfig);
            }
        }
    }

    private void register(final Http11ConfigContext contextConfig) {
        final String realmName = contextConfig.getRealm();
        final Realm realm = (realmName == null) ? null : realms.getRealm(realmName);
        if ((realm != null) || (realmName == null)) {
            final Http11Context context = new Http11Context(contextConfig, realm);
            final Http11ServletContext servletContext = new Http11ServletContext(context);
            context.register(contextConfig.getServlets(), servletContext);
            contexts.put(contextConfig.getPath(), context);
        } else {
            logger.log(Level.SEVERE, realmName);
        }
    }

    public final void doSocket(final Socket socket) throws IOException {
        final Http11Request request = new Http11Request(socket);
/*
        logger.finest(request.getHeader().getRequestLine());
        for (String headerLine : request.getHeader().getHeaderLines()) {
            logger.finest(headerLine);
        }
        final ByteArrayInputStream entity = request.getEntity();
        logger.finest(String.format("[%d]", entity.available()));
*/
        final Http11Response response = new Http11Response(socket);
        final String uri = request.getHeader().getRequestURI();
        if (uri == null) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        } else {
            final Http11Context context = selectContext(uri);
            final Http11Servlet servlet = ((context == null) ? null : context.select(uri));
            if (servlet == null) {
                response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            } else {
                servlet.service(request, response);
            }
        }
        response.write();
        socket.close();
    }

    private Http11Context selectContext(final String uri) {
        Http11Context context = null;
        synchronized (contexts) {
            for (final Map.Entry<String, Http11Context> entry : contexts.entrySet()) {
                final String contextPath = entry.getKey();
                if (uri.startsWith(contextPath)) {
                    context = entry.getValue();
                }
            }
        }
        return context;
    }
}
