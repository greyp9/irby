package io.github.greyp9.irby.core.http11.dispatch;

import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.irby.core.http11.payload.Http11Request;
import io.github.greyp9.irby.core.http11.payload.Http11Response;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletConfig;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletRequest;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletResponse;
import io.github.greyp9.irby.core.realm.Realm;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Http11Servlet {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Http11ServletConfig servletConfig;
    private final Realm realm;
    private final HttpServlet httpServlet;

    public final HttpServlet getHttpServlet() {
        return httpServlet;
    }

    public Http11Servlet(final Http11ServletConfig servletConfig, final Realm realm, final HttpServlet httpServlet) {
        this.servletConfig = servletConfig;
        this.realm = realm;
        this.httpServlet = httpServlet;
    }

    public final void service(final Http11Request http11Request, final Http11Response http11Response) {
        try {
            final Http11Authenticator authenticator = new Http11Authenticator(realm, servletConfig.getConfig());
            final Principal principal = authenticator.authorize(authenticator.authenticate(http11Request));
            final HttpServletRequest request = new Http11ServletRequest(http11Request, servletConfig, principal);
            final HttpServletResponse response = new Http11ServletResponse(http11Response);
            // hand off to servlet implementation
            httpServlet.service(request, response);
        } catch (AccountException e) {
            service(http11Response, e);
        } catch (LoginException e) {
            service(http11Response, e);
        } catch (ServletException e) {
            service(http11Response, e);
        } catch (IOException e) {
            service(http11Response, e);
        }
    }

    private void service(final Http11Response response, final LoginException e) {
        logger.log(Level.FINEST, e.getMessage(), e);
        if (Http.Realm.BASIC.equals(realm.getMethod())) {
            final String challenge = String.format(Http.Realm.BASIC_REALM, realm.getName());
            response.addHeader(Http.Header.WWW_AUTHENTICATE, challenge);
        }
        response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    private void service(final Http11Response response, final AccountException e) {
        logger.log(Level.FINEST, e.getMessage(), e);
        response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
    }

    private void service(final Http11Response response, final ServletException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    private void service(final Http11Response response, final IOException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
}
