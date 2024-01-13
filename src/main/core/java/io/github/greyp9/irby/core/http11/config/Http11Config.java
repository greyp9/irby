package io.github.greyp9.irby.core.http11.config;

import java.util.ArrayList;
import java.util.Collection;

public class Http11Config {
    private final String name;
    private final String host;
    private final int port;
    private final int threads;
    private final long timeout;
    private final Collection<Http11ConfigContext> contexts;

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

    public final long getTimeout() {
        return timeout;
    }

    public final Collection<Http11ConfigContext> getContexts() {
        return contexts;
    }

    public Http11Config(final String name, final String host, final int port, final int threads, final long timeout) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.timeout = timeout;
        this.contexts = new ArrayList<Http11ConfigContext>();
    }

    public final boolean isLocalExecutor() {
        return (threads > 0);
    }

    public final void addContext(final Http11ConfigContext context) {
        contexts.add(context);
    }
}
