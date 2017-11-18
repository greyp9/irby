package io.github.greyp9.irby.core.http11.payload;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.irby.core.http11.servlet25.Http11ServletInputStream;

import javax.servlet.ServletInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

public class Http11Request {
    private final long millis;
    private final Socket socket;
    private final boolean isValid;
    private final Http11Header header;
    private final byte[] entity;

    private String user;

    public final long getMillis() {
        return millis;
    }

    public final Socket getSocket() {
        return socket;
    }

    public boolean isValid() {
        return isValid;
    }

    public final Http11Header getHeader() {
        return header;
    }

    public final ByteArrayInputStream getEntity() {
        return new ByteArrayInputStream((entity == null) ? new byte[0] : entity);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public final ServletInputStream getInputStream() throws IOException {
        return new Http11ServletInputStream(getEntity());
    }

    public Http11Request(final Socket socket) throws IOException {
        this.millis = System.currentTimeMillis();
        this.socket = socket;
        final BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        final byte[] headerBytes = StreamU.readUntil(bis, Const.DELIMITER);
        this.isValid = (headerBytes.length >= Const.DELIMITER.length);
        this.header = (isValid ? new Http11Header(headerBytes) : null);
        this.entity = (isValid ? getEntity(header, bis) : null);
        this.user = null;
    }

    private static byte[] getEntity(final Http11Header header, final BufferedInputStream bis) throws IOException {
        byte[] body = null;
        final String contentLength = header.getHeader(Http.Header.CONTENT_LENGTH);
        if (contentLength != null) {
            final long length = Long.parseLong(contentLength);
            if (length > 0) {
                body = StreamU.read(bis, length);
            }
        }
        return body;
    }

    private static class Const {
        private static final byte[] DELIMITER = UTF8Codec.toBytes(Http.Token.CRLF + Http.Token.CRLF);
    }
}
