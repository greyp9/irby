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
 * keystore config).  Built in XSLT is used to update configuration XML.
 */
public final class ConfigHttpsKeyStore {

    private ConfigHttpsKeyStore() {
    }

    public static void main(final String[] args) throws Exception {
        // query for configuration parameters to be updated
        final Console console = System.console();
        int i = 0;
        final String ksFile = (args.length > ++i) ? args[i] : console.readLine(PROMPT_KS_PATH);
        final char[] ksPass = (args.length > ++i) ? args[i].toCharArray() : console.readPassword(PROMPT_KS_PASSWORD);
        // validate keystore
        new TLSContextFactory().getKeyManager(KS_TYPE_PKCS12, ksFile, ksPass);
        // user feedback
        console.printf("Keystore opened successfully.%n");
        // copy keystore to application conf folder
        final File ksFileIn = new File(ksFile);
        final File ksFileApp = new File("conf", ksFileIn.getName());
        StreamU.write(ksFileApp, StreamU.read(ksFileIn));
        console.printf("Keystore copied successfully.%n");
        // transform "app.xml"
        final URL urlTransform = ResourceU.resolve(XSLT_HTTPS);
        final XsltX xsltX = new XsltX(StreamU.read(urlTransform));
        xsltX.setParameter("keyStoreFile", ksFileApp.getPath())
                .setParameter("keyStoreType", KS_TYPE_PKCS12)
                .setParameter("keyStorePass", new String(ksPass));
        final File fileXml = new File(Application.Const.FILE);
        final byte[] documentSource = StreamU.read(fileXml);
        final byte[] documentTarget = xsltX.transform(documentSource);
        StreamU.write(fileXml, documentTarget);
        console.printf("Configuration update successful.%n");
        System.exit(0);
    }

    private static final String PROMPT_KS_PATH = "Enter path to PKCS12 keystore file: ";
    private static final String PROMPT_KS_PASSWORD = "Enter password to PKCS12 keystore: ";
    private static final String XSLT_HTTPS = "io/github/greyp9/irby/xslt/config-https.xslt";
    private static final String KS_TYPE_PKCS12 = "PKCS12";
}
