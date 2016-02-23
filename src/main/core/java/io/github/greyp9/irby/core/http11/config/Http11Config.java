package io.github.greyp9.irby.core.http11.config;

import java.util.ArrayList;
import java.util.Collection;

public class Http11Config {
    private final String name;
    private final int port;
    private final int threads;
    private final Collection<Http11ConfigContext> contexts;

    public final String getName() {
        return name;
    }

    public final int getPort() {
        return port;
    }

    public final int getThreads() {
        return threads;
    }

    public final Collection<Http11ConfigContext> getContexts() {
        return contexts;
    }

    public Http11Config(final String name, final int port, final int threads) {
        this.name = name;
        this.port = port;
        this.threads = threads;
        this.contexts = new ArrayList<Http11ConfigContext>();
    }

    public final boolean isLocalExecutor() {
        return (threads > 0);
    }

    public final void addContext(final Http11ConfigContext context) {
        contexts.add(context);
    }
}
