package io.github.greyp9.irby.core.realm.config;

import java.util.ArrayList;
import java.util.Collection;

public class RealmConfig {
    private final String name;
    private final String method;
    private final String type;
    private final String context;
    private final Collection<PrincipalConfig> principals;

    public final String getName() {
        return name;
    }

    public final String getMethod() {
        return method;
    }

    public final String getType() {
        return type;
    }

    public final String getContext() {
        return context;
    }

    public final Collection<PrincipalConfig> getPrincipals() {
        return principals;
    }

    public RealmConfig(final String name, final String method, final String type, final String context) {
        this.name = name;
        this.method = method;
        this.type = type;
        this.context = context;
        this.principals = new ArrayList<PrincipalConfig>();
    }

    public final void addPrincipal(final PrincipalConfig principalConfig) {
        principals.add(principalConfig);
    }
}
