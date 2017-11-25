package io.github.greyp9.irby.core.http11.dispatch;

import io.github.greyp9.irby.core.http11.config.Http11ConfigServlet;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletConfig;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletContext;
import io.github.greyp9.irby.core.realm.Realm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Http11ServletFactory {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public final Http11Servlet create(
            final Http11ConfigServlet config, final Http11ServletContext servletContext, final Realm realm) {
        Http11Servlet servlet = null;
        try {
            final Class<?> c = Class.forName(config.getClassName());
            final HttpServlet httpServlet = (HttpServlet) c.newInstance();
            final Http11ServletConfig servletConfig = new Http11ServletConfig(config, servletContext);
            final Http11Authenticator authenticator = new Http11Authenticator(realm, servletConfig.getConfig());
            httpServlet.init(servletConfig);
            servlet = new Http11Servlet(servletConfig, authenticator, httpServlet);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (ServletException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return servlet;
    }
}
