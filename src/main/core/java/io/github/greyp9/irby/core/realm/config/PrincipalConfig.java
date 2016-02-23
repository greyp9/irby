package io.github.greyp9.irby.core.realm.config;

public class PrincipalConfig {
    private final String name;
    private final String credential;
    private final String roles;

    public final String getName() {
        return name;
    }

    public final String getCredential() {
        return credential;
    }

    public final String getRoles() {
        return roles;
    }

    public PrincipalConfig(final String name, final String credential, final String roles) {
        this.name = name;
        this.credential = credential;
        this.roles = roles;
    }
}
