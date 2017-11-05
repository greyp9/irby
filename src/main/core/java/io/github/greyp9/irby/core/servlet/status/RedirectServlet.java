package io.github.greyp9.irby.core.servlet.status;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpResponseU;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RedirectServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = -8363909701936378700L;

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        ServletU.write(HttpResponseU.to302(getInitParameter(Http.Header.LOCATION)), response);
    }
}
