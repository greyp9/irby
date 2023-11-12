package io.github.greyp9.irby.core.tcp.test;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.io.StreamU;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class TCPClientTest {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Disabled
    public final void testSendMessage() throws IOException {
        final Socket socket = new Socket("localhost", 5055);
        final OutputStream os = socket.getOutputStream();
        StreamU.write(os, UTF8Codec.toBytes("hello"));
        logger.info("message written");
        socket.close();
        logger.info("socket closed");
    }

    @Disabled
    public final void testSendMessages() throws IOException {
        final Socket socket = new Socket("localhost", 5055);
        final OutputStream os = socket.getOutputStream();
        os.write(UTF8Codec.toBytes("hello1"));
        os.write(UTF8Codec.toBytes("hello2"));
        os.write(UTF8Codec.toBytes("hello3"));
        os.flush();
        logger.info("messages written");
        socket.close();
        logger.info("socket closed");
    }
}
