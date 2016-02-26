package io.github.greyp9.irby.net.datagram;

import io.github.greyp9.arwo.core.hash.CRCU;
import junit.framework.TestCase;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.logging.Logger;

public class DatagramTest extends TestCase {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //io.github.greyp9.arwo.core.logging.LoggerU.adjust(Logger.getLogger(""));
    }

    public void testSendPacket() throws Exception {
        // setup
        final byte[] bytes = new byte[16];
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
