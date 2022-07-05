package io.github.greyp9.irby.app.transform;

import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.tls.context.TLSContextFactory;
import io.github.greyp9.arwo.core.xslt.XsltX;
import io.github.greyp9.irby.core.app.Application;

import java.io.Console;
import java.io.File;
import java.net.URL;

/**
 * Update application configuration file "conf/app.xml" with externally supplied parameters (TLS
 * truststore config).  Built in XSLT is used to update configuration XML.
 */
public final class ConfigHttpsTrustStore {

    private ConfigHttpsTrustStore() {
    }

    public static void main(final String[] args) throws Exception {
        // query for configuration parameters to be updated
        final Console console = System.console();
        int i = 0;
        final String tsFile = (args.length > ++i) ? args[i] : console.readLine(PROMPT_TS_PATH);
        final char[] tsPass = (args.length > ++i) ? args[i].toCharArray() : console.readPassword(PROMPT_TS_PASSWORD);
        // validate truststore
        new TLSContextFactory().getTrustManager(TS_TYPE_JKS, tsFile, tsPass);
        // user feedback
        console.printf("Truststore opened successfully.%n");
        // copy truststore to application conf folder
        final File tsFileIn = new File(tsFile);
        final File tsFileApp = new File("conf", tsFileIn.getName());
        StreamU.write(tsFileApp, StreamU.read(tsFileIn));
        console.printf("Truststore copied successfully.%n");
        // transform "app.xml"
        final URL urlTransform = ResourceU.resolve(XSLT_HTTPS);
        final XsltX xsltX = new XsltX(StreamU.read(urlTransform));
        xsltX.setParameter("clientTrustFile", tsFileApp.getPath())
                .setParameter("clientTrustType", TS_TYPE_JKS)
                .setParameter("clientTrustPass", new String(tsPass));
        final File fileXml = new File(Application.Const.FILE);
        final byte[] documentSource = StreamU.read(fileXml);
        final byte[] documentTarget = xsltX.transform(documentSource);
        StreamU.write(fileXml, documentTarget);
        console.printf("Configuration update successful.%n");
        System.exit(0);
    }

    private static final String PROMPT_TS_PATH = "Enter path to JKS truststore file: ";
    private static final String PROMPT_TS_PASSWORD = "Enter password to JKS truststore: ";
    private static final String XSLT_HTTPS = "io/github/greyp9/irby/xslt/config-https.xslt";
    private static final String TS_TYPE_JKS = "JKS";
}
