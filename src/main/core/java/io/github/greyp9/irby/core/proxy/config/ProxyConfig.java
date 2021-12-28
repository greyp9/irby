package io.github.greyp9.irby.core.proxy.config;

public class ProxyConfig {
    private final String name;
    private final int port;
    private final int threads;
    private final String host;
    private final String folder;

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

    public final String getFolder() {
        return folder;
    }

    public ProxyConfig(final String name, final int port, final int threads, final String host, final String folder) {
        this.name = name;
        this.port = port;
        this.threads = threads;
        this.host = host;
        this.folder = folder;
    }

    public final boolean isLocalExecutor() {
        return (threads > 0);
    }
}
