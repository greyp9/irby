package io.github.greyp9.irby.core.http11.servlet25;

import java.io.IOException;
import java.io.InputStream;

public class Http11ServletInputStream extends javax.servlet.ServletInputStream {
    private final InputStream inputStream;

    public Http11ServletInputStream(final InputStream inputStream) {
        super();
        this.inputStream = inputStream;
    }

    public final int read() throws IOException {
        return inputStream.read();
    }
}
