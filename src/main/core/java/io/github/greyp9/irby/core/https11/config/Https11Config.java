package io.github.greyp9.irby.core.https11.config;

import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.irby.core.http11.config.Http11Config;

public class Https11Config extends Http11Config {
    private final String type;
    private final String keyStoreFile;
    private final String keyStoreType;
    private final String keyStorePass;
    private final String clientTrustFile;
    private final String clientTrustType;
    private final String clientTrustPass;
    private final String protocol;

    public final String getType() {
        return type;
    }

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

    public final boolean isNeedClientAuth() {
        return Value.notEmpty(clientTrustFile, clientTrustType, clientTrustPass);
    }

    public final String getProtocol() {
        return protocol;
    }

    @SuppressWarnings({ "PMD.ExcessiveParameterList", "checkstyle:parameternumber" })
    public Https11Config(final String type, final String name, final int port, final int threads,
                         final String keyStoreFile, final String keyStoreType, final String keyStorePass,
                         final String clientTrustFile, final String clientTrustType, final String clientTrustPass,
                         final String protocol) {
        super(name, port, threads);
        this.type = type;
        this.keyStoreFile = keyStoreFile;
        this.keyStoreType = keyStoreType;
        this.keyStorePass = keyStorePass;
        this.clientTrustFile = clientTrustFile;
        this.clientTrustType = clientTrustType;
        this.clientTrustPass = clientTrustPass;
        this.protocol = protocol;
    }
}
