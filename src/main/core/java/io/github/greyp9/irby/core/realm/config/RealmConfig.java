package io.github.greyp9.irby.core.realm.config;

import java.util.ArrayList;
import java.util.Collection;

public class RealmConfig {
    private final String name;
    private final String method;
    private final Collection<PrincipalConfig> principals;

    public final String getName() {
        return name;
    }

    public final String getMethod() {
        return method;
    }

    public final Collection<PrincipalConfig> getPrincipals() {
        return principals;
    }

    public RealmConfig(final String name, final String method) {
        this.name = name;
        this.method = method;
        this.principals = new ArrayList<PrincipalConfig>();
    }

    public final void addPrincipal(final PrincipalConfig principalConfig) {
        principals.add(principalConfig);
    }
}
