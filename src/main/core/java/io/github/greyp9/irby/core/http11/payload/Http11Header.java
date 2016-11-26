package io.github.greyp9.irby.core.http11.payload;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.text.line.LineU;
import io.github.greyp9.arwo.core.value.NameTypeValues;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Http11Header {
    private final String requestLine;
    private final Matcher matcher;
    private final Collection<String> headerLines;
    private final NameTypeValues headers;

    @SuppressWarnings("unused")
    public final String getRequestLine() {
        return requestLine;
    }

    @SuppressWarnings("unused")
    public final Collection<String> getHeaderLines() {
        return headerLines;
    }

    public Http11Header(final byte[] bytes) throws IOException {
        final String text = UTF8Codec.toString(bytes);
        final Collection<String> lines = LineU.toLines(text);
        final Iterator<String> iterator = lines.iterator();
        // request line
        this.requestLine = (iterator.hasNext() ? iterator.next() : "");
        lines.remove(this.requestLine);
        lines.remove("");
        final Matcher matcherIt = Const.PATTERN_REQUEST.matcher(this.requestLine);
        this.matcher = (matcherIt.matches() ? matcherIt : null);
        // header lines
        this.headerLines = lines;
        this.headers = new NameTypeValues();
        for (final String line : headerLines) {
            final int index = line.indexOf(Http.Token.COLON);
            if (index > 0) {
                final String name = line.substring(0, index);
                final String value = line.substring(index + Http.Token.COLON.length()).trim();
                this.headers.add(name, value);
            }
        }
    }

    public final String getMethod() {
        return ((matcher == null) ? null : matcher.group(Const.GROUP_METHOD));
    }

    public final String getRequestURI() {
        return ((matcher == null) ? null : matcher.group(Const.GROUP_URI));
    }

    public final String getQueryString() {
        return ((matcher == null) ? null : matcher.group(Const.GROUP_QUERY));
    }

    public final String getProtocol() {
        return ((matcher == null) ? null : matcher.group(Const.GROUP_PROTOCOL));
    }

    public final Collection<String> getHeaderNames() {
        return headers.getNames();
    }

    public final Collection<String> getHeaders(final String name) {
        return headers.getValues(name);
    }

    public final String getHeader(final String name) {
        return headers.getValue(name);
    }

    private static class Const {
        // GET / HTTP/1.1
        private static final String REGEX_REQUEST = "(\\S+)\\s+(\\S+?)(\\?(\\S*))?\\s+(\\S+)";  // i18n internal
        private static final Pattern PATTERN_REQUEST = Pattern.compile(REGEX_REQUEST);
        private static final int GROUP_METHOD = 1;
        private static final int GROUP_URI = 2;
        private static final int GROUP_QUERY = 4;
        private static final int GROUP_PROTOCOL = 5;
    }
}
