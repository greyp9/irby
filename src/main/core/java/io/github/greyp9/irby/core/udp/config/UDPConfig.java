package io.github.greyp9.irby.core.udp.config;

public class UDPConfig {
    private final String name;
    private final String host;
    private final int port;
    private final String target;
    private final int buffer;

    public final String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public final int getPort() {
        return port;
    }

    public final String getTarget() {
        return target;
    }

    public final int getBuffer() {
        return buffer;
    }

    public UDPConfig(final String name, final String host, final int port,
                     final String target, final int buffer) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.target = target;
        this.buffer = buffer;
    }
}
