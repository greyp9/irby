package io.github.greyp9.irby.core.udp.server;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.hash.CRCU;
import io.github.greyp9.irby.core.udp.config.UDPConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPServer {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final UDPConfig config;
    private final AtomicReference<String> reference;

    // lifecycle start/stop
    private DatagramSocket datagramSocket;

    public final UDPConfig getConfig() {
        return config;
    }

    public UDPServer(final UDPConfig config,
                     final AtomicReference<String> reference) {
        this.config = config;
        this.reference = reference;
        this.datagramSocket = null;
    }

    public final void start() throws IOException {
        datagramSocket = startDatagramSocket(config);
        reference.getClass();
    }

    public final void stop() throws IOException {
        if (datagramSocket != null) {
            datagramSocket.close();
        }
    }

    public final void accept() throws IOException {
        try {
            final byte[] bytesReceive = new byte[config.getBuffer()];  // can we declare this at class scope?
            final DatagramPacket packet = new DatagramPacket(bytesReceive, bytesReceive.length);
            datagramSocket.receive(packet);
            doPacket(packet);
        } catch (SocketTimeoutException e) {
            e.getClass();  // ignore; datagramSocket.setSoTimeout()
        }
    }

    private void doPacket(final DatagramPacket packet) {
        final String crc = Long.toHexString(CRCU.crc32(packet.getData(), packet.getOffset(), packet.getLength()));
        logger.log(Level.FINEST, String.format("[%s][%s][%d]", packet.getSocketAddress(), crc, packet.getLength()));
    }

    private static DatagramSocket startDatagramSocket(final UDPConfig config) throws IOException {
        final DatagramSocket datagramSocket = new DatagramSocket(config.getPort());
        datagramSocket.setSoTimeout((int) DurationU.Const.ONE_SECOND_MILLIS);
        return datagramSocket;
    }
}
