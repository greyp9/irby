package io.github.greyp9.irby.net.datagram;

import io.github.greyp9.arwo.core.hash.CRCU;
import org.junit.Before;
import org.junit.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.logging.Logger;

public class DatagramTest {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Before
    public void setUp() throws Exception {
        //io.github.greyp9.arwo.core.logging.LoggerU.adjust(Logger.getLogger(""));
    }

    @Test
    public void testSendPacket() throws Exception {
        // setup
        final int length = 16;
        final byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);
        final String crc = Long.toHexString(CRCU.crc32(bytes));
        final InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("localhost"), 2055);
        final DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
        // test
        final DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        logger.info(String.format("[%s][%s][%d]", packet.getSocketAddress(), crc, packet.getLength()));
        // teardown
        socket.close();
    }
}
