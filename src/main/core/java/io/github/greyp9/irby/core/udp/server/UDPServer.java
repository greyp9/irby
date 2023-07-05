package io.github.greyp9.irby.core.udp.server;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.file.date.FilenameFactory;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.irby.core.udp.config.UDPConfig;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
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
        logger.info(String.format("Service [%s] bound to host [%s], UDP port [%d]",
                config.getName(), config.getHost(), config.getPort()));
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

    private void doPacket(final DatagramPacket packet) throws IOException {
        final File file = FilenameFactory.getUnused(config.getTarget(), new Date());
        StreamU.write(file, packet.getData(), packet.getOffset(), packet.getLength());
        //final String crc = Long.toHexString(CRCU.crc32(packet.getData(), packet.getOffset(), packet.getLength()));
        //logger.log(Level.FINEST, String.format("[%s][%s][%d]", packet.getSocketAddress(), crc, packet.getLength()));
    }

    private static DatagramSocket startDatagramSocket(final UDPConfig config) throws IOException {
        // wildcard address: IPv4=0.0.0.0, IPv6=::1
        final SocketAddress socketAddress = new InetSocketAddress(config.getHost(), config.getPort());
        final DatagramSocket datagramSocket = new DatagramSocket(socketAddress);
        datagramSocket.setSoTimeout((int) DurationU.Const.ONE_SECOND_MILLIS);
        return datagramSocket;
    }
}
