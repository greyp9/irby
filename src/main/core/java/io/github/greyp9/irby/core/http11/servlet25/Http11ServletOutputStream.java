package io.github.greyp9.irby.core.http11.servlet25;

import java.io.IOException;
import java.io.OutputStream;

public class Http11ServletOutputStream extends javax.servlet.ServletOutputStream {
    private final OutputStream outputStream;

    public Http11ServletOutputStream(final OutputStream outputStream) {
        super();
        this.outputStream = outputStream;
    }

    public final void write(final int b) throws IOException {
        outputStream.write(b);
    }
}
