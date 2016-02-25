package io.github.greyp9.irby.core.proxy.config;

public class ProxyConfig {
    private final String name;
    private final int port;
    private final int threads;
    private final String host;

    public final String getName() {
        return name;
    }

    public final int getPort() {
        return port;
    }

    public final int getThreads() {
        return threads;
    }

    public final String getHost() {
        return host;
    }

    public ProxyConfig(final String name, final int port, final int threads, final String host) {
        this.name = name;
        this.port = port;
        this.threads = threads;
        this.host = host;
    }

    public final boolean isLocalExecutor() {
        return (threads > 0);
    }
}
