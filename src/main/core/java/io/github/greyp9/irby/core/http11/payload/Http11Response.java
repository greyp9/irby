package io.github.greyp9.irby.core.http11.payload;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.lang.NumberU;
import io.github.greyp9.arwo.core.value.NameTypeValue;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletOutputStream;

import javax.servlet.ServletOutputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Properties;

public class Http11Response {
    private final Socket socket;
    private final NameTypeValues headers;
    private final Properties properties;
    private final ByteArrayOutputStream entity;

    public final Socket getSocket() {
        return socket;
    }

    public Http11Response(final Socket socket) {
        this.socket = socket;
        this.headers = new NameTypeValues();
        this.properties = new Properties();
        this.entity = new ByteArrayOutputStream();
    }

    public final void addHeader(final String name, final Object value) {
        headers.add(new NameTypeValue(name, value));
    }

    public final ServletOutputStream getOutputStream() throws IOException {
        return new Http11ServletOutputStream(entity);
    }

    public final int getStatus() {
        return NumberU.toInt(properties.getProperty(""), 0);
    }

    public final int getSize() {
        return entity.size();
    }

    public final void setStatus(final int status) {
        properties.setProperty("", Integer.toString(status));
    }

    public final void write() throws IOException {
        final StringBuilder buffer = new StringBuilder();
        final String statusCode = properties.getProperty("");
        final boolean httpOK = Integer.toString(HttpURLConnection.HTTP_OK).equals(statusCode);
        final String statusLine = String.format(Const.STATUS_LINE, statusCode, Http.Token.CRLF);
        buffer.append(statusLine);
        for (final NameTypeValue header : headers) {
            buffer.append(String.format(Const.HEADER, header.getName(), header.getValueS(), Http.Token.CRLF));
        }
        buffer.append(Http.Token.CRLF);
        final BufferedOutputStream os = new BufferedOutputStream(socket.getOutputStream());
        os.write(UTF8Codec.toBytes(buffer.toString()));
        if (httpOK || (entity.size() > 0)) {
            os.write(entity.toByteArray());
        } else {
            os.write(UTF8Codec.toBytes(statusLine));
        }
        os.close();
    }

    private static class Const {
        private static final String STATUS_LINE = "HTTP/1.1 %s%s";
        private static final String HEADER = "%s: %s%s";
    }
}
