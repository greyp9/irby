package io.github.greyp9.irby.core.cron.impl.net;

import io.github.greyp9.arwo.core.cer.CertificateU;
import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.file.date.FilenameFactory;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpRequest;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.httpclient.HttpClientU;
import io.github.greyp9.arwo.core.httpclient.HttpsClient;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.net.ProxyU;
import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.arwo.core.value.NTV;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.arwo.core.xml.ElementU;
import io.github.greyp9.irby.core.cron.impl.CronRunnable;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({ "PMD.DoNotUseThreads", "unused" })
public class HttpRunnable extends CronRunnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public HttpRunnable(String name, Date date, Element element) {
        super(name, date, element);
    }

    @Override
    public final void run() {
        final String className = getClass().getName();
        final String date = XsdDateU.toXSDZMillis(getDate());
        final String methodName = String.format("run(%s)", date);
        logger.entering(className, methodName);
        final String protocol = ElementU.getAttribute(getElement(), Const.PROTOCOL);
        final String certificate = ElementU.getAttribute(getElement(), Const.CERTIFICATE);
        final String proxy = ElementU.getAttribute(getElement(), Const.PROXY);
        final String method = ElementU.getAttribute(getElement(), Const.METHOD);
        final String authorization = ElementU.getAttribute(getElement(), Const.AUTHORIZATION);
        final String sourceUrl = ElementU.getAttribute(getElement(), Const.SOURCE_URL);
        final String targetFile = ElementU.getAttribute(getElement(), Const.TARGET_FILE);
        logger.log(Level.FINEST, String.format("[%s][%s][%s]",
                XsdDateU.toXSDZMillis(getDate()), sourceUrl, targetFile));
        try {
            new Job(getDate(), protocol, certificate, proxy, method, authorization, sourceUrl, targetFile).execute();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        } catch (GeneralSecurityException e) {
            logger.severe(e.getMessage());
        }
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String PROTOCOL = "protocol";  // i18n internal
        private static final String CERTIFICATE = "certificate";  // i18n internal
        private static final String PROXY = "proxy";  // i18n internal
        private static final String METHOD = "method";  // i18n internal
        private static final String AUTHORIZATION = "authorization";  // i18n internal
        private static final String SOURCE_URL = "source-url";  // i18n internal
        private static final String TARGET_FILE = "target-file";  // i18n internal
    }

    private static class Job {
        private final Logger logger = Logger.getLogger(getClass().getName());

        private final Date date;
        private final String protocol;
        private final String certificate;
        private final String proxy;
        private final String method;
        private final String authorization;
        private final String sourceUrl;
        private final String targetFile;

        public Job(final Date date, final String protocol, final String certificate, final String proxy,
                   final String method, final String authorization, final String sourceUrl, final String targetFile) {
            this.date = date;
            this.protocol = protocol;
            this.certificate = certificate;
            this.proxy = proxy;
            this.method = method;
            this.authorization = authorization;
            this.sourceUrl = sourceUrl;
            this.targetFile = targetFile;
        }

        public void execute() throws IOException, GeneralSecurityException {
            final X509Certificate certificate = Value.isEmpty(this.certificate) ? null :
                    CertificateU.toX509(UTF8Codec.toBytes(this.certificate));
            final URL urlProxy = Value.isEmpty(proxy) ? null : URLCodec.toURL(proxy);
            final Proxy proxyRunnable = ProxyU.toHttpProxy(urlProxy);
            final HttpsClient httpsClient = new HttpsClient(certificate, false, proxyRunnable);
            final URL url = new URL(sourceUrl);
            final NameTypeValues headersRequest = NTV.create(
                    Http.Header.AUTHORIZATION, HttpClientU.toBasicAuth(authorization));
            final HttpRequest httpRequest = new HttpRequest(
                    method, url.getFile(), url.getQuery(), headersRequest, null);
            final HttpResponse httpResponse = httpsClient.doRequest(url, httpRequest);
            final byte[] responseEntity = StreamU.read(httpResponse.getEntity());
            final File file = FilenameFactory.getUnused(targetFile, date);
            logger.info(file.getAbsolutePath());
            StreamU.writeMkdirs(file, responseEntity);
        }
    }
}
