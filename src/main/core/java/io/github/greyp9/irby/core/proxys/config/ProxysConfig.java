package io.github.greyp9.irby.core.proxys.config;

import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.irby.core.proxy.config.ProxyConfig;

public class ProxysConfig extends ProxyConfig {
    private final String keyStoreFile;
    private final String keyStoreType;
    private final String keyStorePass;
    private final String clientTrustFile;
    private final String clientTrustType;
    private final String clientTrustPass;
    private final String serverTrustFile;
    private final String protocol;

    public final String getKeyStoreFile() {
        return keyStoreFile;
    }

    public final String getKeyStoreType() {
        return keyStoreType;
    }

    public final String getKeyStorePass() {
        return keyStorePass;
    }

    public final String getClientTrustFile() {
        return clientTrustFile;
    }

    public final String getClientTrustType() {
        return clientTrustType;
    }

    public final String getClientTrustPass() {
        return clientTrustPass;
    }

    public final String getServerTrustFile() {
        return serverTrustFile;
    }

    public final boolean isNeedClientAuth() {
        return Value.notEmpty(clientTrustFile, clientTrustType, clientTrustPass);
    }

    public final String getProtocol() {
        return protocol;
    }

    @SuppressWarnings({ "PMD.ExcessiveParameterList", "checkstyle:parameternumber" })
    public ProxysConfig(final String name, final int port, final int threads, final String host,
                        final String keyStoreFile, final String keyStoreType, final String keyStorePass,
                        final String clientTrustFile, final String clientTrustType, final String clientTrustPass,
                        final String serverTrustFile, final String protocol, final String folder) {
        super(name, port, threads, host, folder);
        this.keyStoreFile = keyStoreFile;
        this.keyStoreType = keyStoreType;
        this.keyStorePass = keyStorePass;
        this.clientTrustFile = clientTrustFile;
        this.clientTrustType = clientTrustType;
        this.clientTrustPass = clientTrustPass;
        this.serverTrustFile = serverTrustFile;
        this.protocol = protocol;
    }
}
