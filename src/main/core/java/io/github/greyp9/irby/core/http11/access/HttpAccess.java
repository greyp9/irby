package io.github.greyp9.irby.core.http11.access;

import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.value.Value;

import java.util.Date;

/**
 * https://en.wikipedia.org/wiki/Common_Log_Format
 */
public class HttpAccess {
    private final String remoteHost;
    private final String rfc1413;
    private final String remoteUser;
    private final Date date;
    private final String requestLine;
    private final int status;
    private final int size;

    public HttpAccess(String remoteHost, String rfc1413, String remoteUser, Date date,
                      String requestLine, int status, int size) {
        this.remoteHost = remoteHost;
        this.rfc1413 = rfc1413;
        this.remoteUser = remoteUser;
        this.date = date;
        this.requestLine = requestLine;
        this.status = status;
        this.size = size;
    }

    @Override
    public String toString() {
        final String field1413 = Value.defaultOnNull(rfc1413, Http.Token.HYPHEN);
        final String fieldUser = Value.defaultOnNull(remoteUser, Http.Token.HYPHEN);
        final String fieldDate = Value.wrap("[", "]", XsdDateU.toXSDZMillis(date));
        final String fieldLine = Value.wrap(Http.Token.QUOTE, requestLine);
        final String fieldStatus = Integer.toString(status);
        final String fieldSize = (size > 0) ? Integer.toString(size) : Http.Token.HYPHEN;
        return Value.join(Http.Token.SPACE, remoteHost, field1413, fieldUser, fieldDate,
                fieldLine, fieldStatus, fieldSize);
    }
}
