package io.github.greyp9.irby.core.widget.net.http;

import io.github.greyp9.arwo.core.codec.b64.Base64Codec;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.file.date.FilenameFactory;
import io.github.greyp9.arwo.core.hash.secure.HashU;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpRequest;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.httpclient.HttpClientU;
import io.github.greyp9.arwo.core.httpclient.HttpsClient;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.tls.client.CertificateClient;
import io.github.greyp9.arwo.core.value.NTV;
import io.github.greyp9.arwo.core.value.NameTypeValues;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class WgetRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final String name;
    private final Date date;
    private final String urlFrom;
    private final String protocol;
    private final String certificateHash;
    private final String method;
    private final String authorization;
    private final String fileTo;

    @SuppressWarnings("checkstyle:magicnumber")
    public WgetRunnable(final String name, final Date date, final String... params) {
        this(name, date, params[0], params[1], params[2], params[3], params[4], params[5]);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public WgetRunnable(final String name, final Date date,
                        final String url, final String protocol, final String certificateHash,
                        final String method, final String authorization, final String fileTo) {
        this.name = name;
        this.date = date;
        this.urlFrom = url;
        this.protocol = protocol;
        this.certificateHash = certificateHash;
        this.method = method;
        this.authorization = authorization;
        this.fileTo = fileTo;
    }

    @Override
    public void run() {
        final String className = getClass().getName();
        final String methodName = "run()";
        logger.entering(className, methodName);
        try {
            httpsMethod();
            logger.fine(String.format("%s:[%s]:%s", name, XsdDateU.toXSDZMillis(date), urlFrom));
        } catch (IOException | GeneralSecurityException e) {
            logger.severe(e.getMessage());
        }
        logger.exiting(className, methodName);
    }

    private void httpsMethod() throws IOException, GeneralSecurityException {
        final URL url = new URL(urlFrom);
        final CertificateClient client = new CertificateClient(protocol);
        final Collection<X509Certificate> certificates = client.getCertificateChain(url);
        final X509Certificate certificate = certificates.iterator().next();
        final String certificateHashRemote = Base64Codec.encode(HashU.sha256(certificate.getEncoded()));
        if ((certificateHash != null) && (certificateHash.equals(certificateHashRemote))) {
            httpsMethodTrustServer(url, certificate);
        } else {
            throw new GeneralSecurityException(String.format("%s != %s", certificateHash, certificateHashRemote));
        }
    }

    private void httpsMethodTrustServer(final URL url, final X509Certificate certificate)
            throws IOException, GeneralSecurityException {
        final NameTypeValues headersRequest = NTV.create(
                Http.Header.AUTHORIZATION, HttpClientU.toBasicAuth(authorization));
        final HttpRequest httpRequest = new HttpRequest(method, url.getFile(), url.getQuery(), headersRequest, null);
        final HttpsClient httpsClient = new HttpsClient(certificate, false);
        final HttpResponse httpResponse = httpsClient.doRequest(url, httpRequest);
        final byte[] responseEntity = StreamU.read(httpResponse.getEntity());
        final File file = FilenameFactory.getUnused(fileTo, date);
        logger.info(file.getAbsolutePath());
        StreamU.writeMkdirs(file, responseEntity);
    }
}
