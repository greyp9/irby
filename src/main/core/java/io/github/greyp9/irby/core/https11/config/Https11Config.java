package io.github.greyp9.irby.core.https11.config;

import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.irby.core.http11.config.Http11Config;

public class Https11Config extends Http11Config {
    private final String keyStoreFile;
    private final String keyStoreType;
    private final String keyStorePass;
    private final String trustStoreFile;
    private final String trustStoreType;
    private final String trustStorePass;
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

    public final String getTrustStoreFile() {
        return trustStoreFile;
    }

    public final String getTrustStoreType() {
        return trustStoreType;
    }

    public final String getTrustStorePass() {
        return trustStorePass;
    }

    public final boolean isNeedClientAuth() {
        return Value.notEmpty(trustStoreFile, trustStoreType, trustStorePass);
    }

    public final String getProtocol() {
        return protocol;
    }

    @SuppressWarnings({ "PMD.ExcessiveParameterList", "checkstyle:parameternumber" })
    public Https11Config(final String name, final int port, final int threads,
                         final String keyStoreFile, final String keyStoreType, final String keyStorePass,
                         final String trustStoreFile, final String trustStoreType, final String trustStorePass,
                         final String protocol) {
        super(name, port, threads);
        this.keyStoreFile = keyStoreFile;
        this.keyStoreType = keyStoreType;
        this.keyStorePass = keyStorePass;
        this.trustStoreFile = trustStoreFile;
        this.trustStoreType = trustStoreType;
        this.trustStorePass = trustStorePass;
        this.protocol = protocol;
    }
}
