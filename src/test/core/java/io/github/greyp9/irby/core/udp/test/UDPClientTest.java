package io.github.greyp9.irby.core.udp.test;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.hash.CRCU;
import io.github.greyp9.arwo.core.lang.NumberU;
import org.junit.Assert;
import org.junit.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.logging.Logger;

public class UDPClientTest {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Test
    public void testSendPacket() throws Exception {
        final String className = getClass().getName();
        final String methodName = "testSendOnePacket()";  // i18n trace
        logger.entering(className, methodName);
        final byte[] bytes = UTF8Codec.toBytes(XsdDateU.toXSDZMillis(new Date()));
        Assert.assertNotNull(bytes);
        final int crc = (int) CRCU.crc32(bytes);
        final InetAddress inetAddress = InetAddress.getLocalHost();  // InetAddress.getByName("127.0.0.1");
        logger.finest(String.format("[%s][%s][%d]", inetAddress.toString(), NumberU.toHex(crc), bytes.length));
        final DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, inetAddress, 2055);
        final DatagramSocket socket = new DatagramSocket();
        for (int i = 0; (i < 1); ++i) {
            socket.send(packet);
        }
        logger.exiting(className, methodName);
    }
}
