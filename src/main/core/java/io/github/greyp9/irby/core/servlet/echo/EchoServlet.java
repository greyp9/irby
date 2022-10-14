package io.github.greyp9.irby.core.servlet.echo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Enumeration;

public class EchoServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = -7034326698100242323L;

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final StringBuilder buffer = new StringBuilder();
        final String method = request.getMethod();
        final String requestURI = request.getRequestURI();
        final String queryString = request.getQueryString();
        final String protocol = request.getProtocol();
        buffer.append(String.format("%s %s%s %s%n", method, requestURI,
                ((queryString == null) ? "" : queryString), protocol));
        final Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = (String) headerNames.nextElement();
            final String value = request.getHeader(name);
            buffer.append(String.format("%s: %s%n", name, value));
        }
        final byte[] bytes = buffer.toString().getBytes("UTF-8");  // i18n http
        response.setStatus(HttpURLConnection.HTTP_OK);
        response.setContentType("text/plain; charset='UTF-8'");  // i18n http
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }
}
