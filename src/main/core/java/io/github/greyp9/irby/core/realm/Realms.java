package io.github.greyp9.irby.core.realm;

import io.github.greyp9.irby.core.realm.config.RealmConfig;
import io.github.greyp9.irby.core.realm.impl.ArwoRealm;
import io.github.greyp9.irby.core.realm.impl.SimpleRealm;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Realms {
    private final Map<String, Realm> mapRealms;

    public final Realm getRealm(final String key) {
        return mapRealms.get(key);
    }

    public Realms(final Collection<RealmConfig> realmConfigs) {
        this.mapRealms = new TreeMap<String, Realm>();
        for (final RealmConfig realmConfig : realmConfigs) {
            mapRealms.put(realmConfig.getName(), createRealm(realmConfig));
        }
    }

    @SuppressWarnings("PMD.AvoidFinalLocalVariable")
    private Realm createRealm(final RealmConfig realmConfig) {
        final boolean isArwoRealm = "arwo".equals(realmConfig.getType());
        return (isArwoRealm ? ArwoRealm.create(realmConfig) : SimpleRealm.create(realmConfig));
    }
}
