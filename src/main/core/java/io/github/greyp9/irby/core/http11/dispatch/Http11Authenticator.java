package io.github.greyp9.irby.core.http11.dispatch;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.codec.b64.Base64Codec;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.irby.core.http11.config.Http11ConfigServlet;
import io.github.greyp9.irby.core.http11.payload.Http11Request;
import io.github.greyp9.irby.core.realm.Realm;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.Socket;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Http11Authenticator {
    private final Realm realm;
    private final String authConstraint;

    public Http11Authenticator(final Realm realm, final Http11ConfigServlet configServlet) {
        this.realm = realm;
        this.authConstraint = configServlet.getAuthConstraint();
    }

    public final Principal authenticate(final Http11Request request) throws LoginException, IOException {
        Principal principal;
        if (realm == null) {
            principal = null;
        } else if (authConstraint == null) {
            principal = null;
        } else if (Http.Realm.BASIC.equals(realm.getMethod())) {
            principal = authenticateBasic(request);
        } else if (Http.Realm.CERTIFICATE.equals(realm.getMethod())) {
            principal = authenticateCertificate(request);
        } else {
            throw new LoginException(realm.getMethod());
        }
        return principal;
    }

    private Principal authenticateBasic(final Http11Request request) throws IOException {
        final String header = request.getHeader().getHeader(Http.Header.AUTHORIZATION);
        final Matcher matcher = Const.REGEX_BASIC.matcher(header == null ? "" : header);
        final String base64 = (matcher.matches()) ? matcher.group(1) : null;
        return authenticateBasic(UTF8Codec.toString(Base64Codec.decode(base64)));
    }

    private Principal authenticateBasic(final String headerDecoded) throws IOException {
        final int index = (headerDecoded == null) ? -1 : headerDecoded.indexOf(Http.Token.COLON);
        final String user = (index < 0) ? headerDecoded : headerDecoded.substring(0, index);
        final String password = (index < 0) ? "" : headerDecoded.substring(index + Http.Token.COLON.length());
        return realm.authenticate(user, password);
    }

    private Principal authenticateCertificate(final Http11Request request) throws AccountException {
        final Certificate[] certificates = getPeerCertificates(request.getSocket());
        final X509Certificate peer = (X509Certificate) certificates[0];
        return realm.authenticate(peer.getSubjectX500Principal().getName(), peer);
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    private Certificate[] getPeerCertificates(final Socket socket) throws AccountException {
        if (socket instanceof SSLSocket) {
            final SSLSocket sslSocket = (SSLSocket) socket;
            try {
                return sslSocket.getSession().getPeerCertificates();
            } catch (SSLException e) {
                throw new AccountException(e.getMessage());
            }
        } else {
            throw new AccountException(socket.getClass().getName());
        }
    }

    public final Principal authorize(final Principal principal) throws LoginException {
        Principal authorized;
        if (authConstraint == null) {
            authorized = principal;
        } else if (principal == null) {
            throw new LoginException(authConstraint);
        } else if (realm.isUserInRole(principal, authConstraint)) {
            authorized = principal;
        } else {
            throw new AccountException(principal.getName() + Http.Token.SLASH + authConstraint);
        }
        return authorized;
    }

    private static class Const {
        private static final Pattern REGEX_BASIC = Pattern.compile(String.format("%s (.+)", Http.Realm.BASIC));
    }
}
