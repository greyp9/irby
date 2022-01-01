package io.github.greyp9.irby.core.udp.test;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.date.DateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.hash.CRCU;
import io.github.greyp9.arwo.core.lang.NumberU;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.logging.Logger;

public class UDPClient2Test {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Test
    public void testSendPackets() throws Exception {
        final String className = getClass().getName();
        final String methodName = "testSendPackets()";  // i18n trace
        logger.entering(className, methodName);
        Date date = new Date();
        final Date dateEnd = DurationU.add(new Date(), DateU.Const.TZ_GMT, DurationU.Const.ONE_SECOND);
        while (date.getTime() < dateEnd.getTime()) {
            ThreadU.sleepMillis(DurationU.Const.ONE_SECOND_MILLIS);
            sendOnePacket();
            date = new Date();
        }
        logger.exiting(className, methodName);
    }

    private void sendOnePacket() throws IOException {
        final byte[] bytes = UTF8Codec.toBytes(XsdDateU.toXSDZMillis(new Date()));  // i18n payload
        Assert.assertNotNull(bytes);
        final int crc = (int) CRCU.crc32(bytes);
        final InetAddress inetAddress = InetAddress.getLocalHost();  // InetAddress.getByName("127.0.0.1");
        logger.finest(String.format("[%s][%s][%d]", inetAddress.toString(), NumberU.toHex(crc), bytes.length));
        final DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, inetAddress, 2055);
        final DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
    }
}
