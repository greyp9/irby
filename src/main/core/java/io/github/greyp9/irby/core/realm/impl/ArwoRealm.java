package io.github.greyp9.irby.core.realm.impl;

import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.security.realm.AppRealm;
import io.github.greyp9.arwo.core.security.realm.AppRealmContainer;
import io.github.greyp9.arwo.core.security.realm.AuthPrincipal;
import io.github.greyp9.irby.core.realm.Realm;
import io.github.greyp9.irby.core.realm.config.RealmConfig;

import java.security.Principal;
import java.util.ArrayList;

public final class ArwoRealm implements Realm, AppRealmContainer {
    //private final Logger logger = Logger.getLogger(getClass().getName());

    private final RealmConfig config;
    private AppRealm appRealm = new AppRealm("", "", new ArrayList<AuthPrincipal>());

    private ArwoRealm(final RealmConfig config) {
        this.config = config;
        //this.appRealm = AppRealmFactory.toAppRealm(config.getContext());
        final javax.naming.Context context = AppNaming.createSubcontext(config.getContext());
        //final Object o = AppNaming.lookup(context, AppRealmContainer.NAMING_CONTAINER);
        AppNaming.bind(context, NAMING_CONTAINER, this);
    }

    @Override
    public String getName() {
        return appRealm.getName();
    }

    @Override
    public String getMethod() {
        return config.getMethod();
    }

    @Override
    public Principal authenticate(final String user, final Object credentials) {
        return appRealm.authenticate(user, credentials);
    }

    @Override
    public boolean isUserInRole(final Principal user, final String role) {
        return appRealm.isUserInRole(user, role);
    }

    @Override
    public void setAppRealm(final AppRealm appRealm) {
        if (appRealm == null) {
            this.appRealm.clear();
            this.appRealm = null;
        } else {
            this.appRealm = appRealm;
        }
    }

    public static Realm create(final RealmConfig realmConfig) {
        return new ArwoRealm(realmConfig);
    }
}
