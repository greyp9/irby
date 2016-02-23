package io.github.greyp9.irby.core.realm;

import java.security.Principal;

public interface Realm {
    String getName();

    String getMethod();

    Principal authenticate(String user, Object credentials);

    boolean isUserInRole(Principal user, String role);
}
