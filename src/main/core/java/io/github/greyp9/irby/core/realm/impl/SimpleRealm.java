package io.github.greyp9.irby.core.realm.impl;

import io.github.greyp9.arwo.core.codec.hex.HexCodec;
import io.github.greyp9.arwo.core.hash.secure.HashU;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.security.realm.AppPrincipal;
import io.github.greyp9.irby.core.realm.Realm;
import io.github.greyp9.irby.core.realm.config.PrincipalConfig;
import io.github.greyp9.irby.core.realm.config.RealmConfig;

import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleRealm implements Realm {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final RealmConfig config;

    private SimpleRealm(final RealmConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public String getMethod() {
        return config.getMethod();
    }

    @Override
    public Principal authenticate(final String user, final Object credential) {
        Principal principal = null;
        final String method = config.getMethod();
        final boolean isPass = (Http.Realm.BASIC.equals(method) && (credential instanceof String));
        final boolean isCert = (Http.Realm.CERTIFICATE.equals(method) && (credential instanceof X509Certificate));
        if (isPass) {
            principal = authenticatePassword(user, (String) credential);
        } else if (isCert) {
            principal = authenticateCertificate(user, (X509Certificate) credential);
        }
        return principal;
    }

    private Principal authenticatePassword(final String user, final String credential) {
        Principal principal = null;
        final Iterator<PrincipalConfig> it = config.getPrincipals().iterator();
        while ((it.hasNext()) && (principal == null)) {
            principal = authenticate(it.next(), user, credential);
        }
        return principal;
    }

    private Principal authenticateCertificate(final String user, final X509Certificate credential) {
        Principal principal = null;
        final String fingerprint = toFingerprint(credential);
        final Iterator<PrincipalConfig> it = config.getPrincipals().iterator();
        while ((it.hasNext()) && (principal == null)) {
            principal = authenticate(it.next(), user, fingerprint);
        }
        final Level level = ((principal == null) ? Level.INFO : Level.FINEST);
        logger.log(level, () -> String.format("credential=[%s] name=[%s]", fingerprint, user));
        return principal;
    }

    private static Principal authenticate(final PrincipalConfig principal, final String user, final String credential) {
        final boolean nameMatch = principal.getName().equals(user);
        final boolean credentialMatch = (nameMatch && principal.getCredential().equals(credential));
        return ((nameMatch && credentialMatch) ? toPrincipal(principal) : null);
    }

    private static AppPrincipal toPrincipal(final PrincipalConfig principal) {
        return new AppPrincipal(principal.getName(), Collections.singleton(principal.getRoles()));
    }

    private static String toFingerprint(final X509Certificate credential) {
        String fingerprint;
        try {
            fingerprint = HexCodec.encode(HashU.sha256(credential.getEncoded()));
        } catch (CertificateEncodingException e) {
            fingerprint = null;
        }
        return fingerprint;
    }

    @Override
    public boolean isUserInRole(final Principal principal, final String role) {
        boolean authorized = false;
        if (principal instanceof AppPrincipal) {
            authorized = ((AppPrincipal) principal).getRoles().contains(role);
        }
        return authorized;
    }

    public static SimpleRealm create(final RealmConfig realmConfig) {
        return new SimpleRealm(realmConfig);
    }
}
