package io.github.greyp9.irby.core.http11.payload;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.io.StreamU;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

public class Http11Request {
    private final Socket socket;
    private final Http11Header header;
    private final byte[] entity;

    public final Socket getSocket() {
        return socket;
    }

    public final Http11Header getHeader() {
        return header;
    }

    public final ByteArrayInputStream getEntity() {
        return new ByteArrayInputStream((entity == null) ? new byte[0] : entity);
    }

    public Http11Request(final Socket socket) throws IOException {
        this.socket = socket;
        final BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        final byte[] headerBytes = StreamU.readUntil(bis, Const.DELIMITER);
        this.header = new Http11Header(headerBytes);
        this.entity = getEntity(header, bis);
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
