package io.github.greyp9.irby.app.transform;

import io.github.greyp9.arwo.core.envsec.EnvironmentSecret;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.jce.AES;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.tls.context.TLSContextFactory;
import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.arwo.core.value.NameTypeValuesU;
import io.github.greyp9.arwo.core.xed.transform.TransformContext;
import io.github.greyp9.arwo.core.xed.transform.ValueInstanceTransform;
import io.github.greyp9.arwo.core.xsd.instance.TypeInstance;
import io.github.greyp9.arwo.core.xsd.model.XsdTypes;
import io.github.greyp9.arwo.core.xsd.value.ValueInstance;
import io.github.greyp9.arwo.core.xslt.XsltX;
import io.github.greyp9.irby.core.Irby;
import io.github.greyp9.irby.core.app.Application;
import io.github.greyp9.irby.core.app.config.ApplicationConfig;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
        // validate truststore (user feedback)
        new TLSContextFactory().getTrustManager(TS_TYPE_JKS, tsFile, tsPass);
        console.printf("Truststore opened successfully.%n");
        // protect password
        final File fileXml = new File(Application.Const.FILE);
        final ApplicationConfig config = new ApplicationConfig(URLCodec.toURL(fileXml));
        final String secretPath = config.getSecret();
        final byte[] secret = (secretPath == null) ? null : new EnvironmentSecret(secretPath, null).recover();
        final SecretKey keyApp = new SecretKeySpec(secret, AES.Const.ALGORITHM);
        final XsdTypes xsdTypes = new XsdTypes(ResourceU.resolve(Irby.App.XSD));
        final TypeInstance typeHttps11 = xsdTypes.getElementType(Irby.App.QNAME.toString()).getInstance(TYPE_HTTPS11);
        final NameTypeValues ntvIn = NameTypeValuesU.create(TS_PASS, new String(tsPass));
        final ValueInstance valueInstance = ValueInstance.create(typeHttps11, ntvIn);
        final TransformContext context = new TransformContext(keyApp, null);
        final ValueInstance valueInstanceX = new ValueInstanceTransform(context).transform(valueInstance);
        final char[] tsPassProtect = valueInstanceX.getNameTypeValue(
                typeHttps11.getInstance(TS_PASS)).getValueS().toCharArray();
        // copy truststore to application conf folder
        final File tsFileIn = new File(tsFile);
        final File tsFileApp = new File(Irby.FS.CONF, tsFileIn.getName());
        StreamU.write(tsFileApp, StreamU.read(tsFileIn));
        console.printf("Truststore copied successfully.%n");
        // transform "app.xml"
        final URL urlTransform = ResourceU.resolve(XSLT_HTTPS);
        final XsltX xsltX = new XsltX(StreamU.read(urlTransform));
        xsltX.setParameter(TS_FILE, tsFileApp.getPath())
                .setParameter(TS_TYPE, TS_TYPE_JKS)
                .setParameter(TS_PASS, new String(tsPassProtect));
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

    private static final String TYPE_HTTPS11 = "https11";
    private static final String TS_TYPE = "clientTrustType";
    private static final String TS_FILE = "clientTrustFile";
    private static final String TS_PASS = "clientTrustPass";
}
