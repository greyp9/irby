package io.github.greyp9.irby.core.udp.test;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.hash.CRCU;
import io.github.greyp9.arwo.core.lang.NumberU;
import junit.framework.TestCase;
import org.junit.Assert;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class UDPClientTest extends TestCase {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public void testSendPacket() throws Exception {
        final String className = getClass().getName();
        final String methodName = "testSendOnePacket()";  // i18n trace
        logger.entering(className, methodName);
        final byte[] bytes = UTF8Codec.toBytes("abc123");  // i18n payload
        Assert.assertNotNull(bytes);
        final int crc = (int) CRCU.crc32(bytes);
        final InetAddress inetAddress = InetAddress.getLocalHost();
        logger.finest(String.format("[%s][%s][%d]", inetAddress.toString(), NumberU.toHex(crc), bytes.length));
        final DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, inetAddress, 2055);
        final DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        logger.exiting(className, methodName);
    }
}
