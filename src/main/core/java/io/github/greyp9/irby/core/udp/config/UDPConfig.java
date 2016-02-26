package io.github.greyp9.irby.core.udp.config;

public class UDPConfig {
    private final String name;
    private final int port;
    private final int buffer;

    public final String getName() {
        return name;
    }

    public final int getPort() {
        return port;
    }

    public final int getBuffer() {
        return buffer;
    }

    public UDPConfig(final String name, final int port, final int buffer) {
        this.name = name;
        this.port = port;
        this.buffer = buffer;
    }
}
