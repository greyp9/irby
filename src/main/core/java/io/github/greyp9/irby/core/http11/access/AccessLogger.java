package io.github.greyp9.irby.core.http11.access;

import io.github.greyp9.irby.core.http11.payload.Http11Request;
import io.github.greyp9.irby.core.http11.payload.Http11Response;

import java.util.Date;
import java.util.logging.Logger;

public class AccessLogger {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public final void log(long millis, Http11Request request, Http11Response response) {
        final String host = request.getSocket().getInetAddress().getHostAddress();
        final String user = request.getUser();
        final Date date = new Date(millis);
        final String requestLine = request.getHeader().getRequestLine();
        final int status = response.getStatus();
        final int size = response.getSize();
        final HttpAccess httpAccess = new HttpAccess(host, null, user, date, requestLine, status, size);
        logger.fine(httpAccess.toString());
    }
}
