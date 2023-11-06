package io.github.greyp9.irby.core.tcp.config;

public class TCPConfig {
    private final String name;
    private final String host;
    private final int port;
    private final int threads;
    private final String target;
    private final int buffer;

    public final String getName() {
        return name;
    }

    public final String getHost() {
        return host;
    }

    public final int getPort() {
        return port;
    }

    public final int getThreads() {
        return threads;
    }

    public final String getTarget() {
        return target;
    }

    public final int getBuffer() {
        return buffer;
    }

    public TCPConfig(final String name, final String host, final int port, final int threads,
                     final String target, final int buffer) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.target = target;
        this.buffer = buffer;
    }
}
