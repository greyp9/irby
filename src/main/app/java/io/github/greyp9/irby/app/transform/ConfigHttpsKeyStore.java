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
 * keystore config).  Built in XSLT is used to update configuration XML.
 */
public final class ConfigHttpsKeyStore {

    private ConfigHttpsKeyStore() {
    }

    public static void main(final String[] args) throws Exception {
        // query for configuration parameters to be updated
        final Console console = System.console();
        int i = 0;
        final String ksFile = (args.length >= ++i) ? args[i] : console.readLine(PROMPT_KS_PATH);
        final char[] ksPass = (args.length >= ++i) ? args[i].toCharArray() : console.readPassword(PROMPT_KS_PASSWORD);
        final File fileXml = new File(Application.Const.FILE);
        final ApplicationConfig config = new ApplicationConfig(URLCodec.toURL(fileXml));
        final String secretPath = config.getSecret();
        final byte[] secret = (secretPath == null) ? null : new EnvironmentSecret(secretPath, null).recover();
        final SecretKey keyApp = new SecretKeySpec(secret, AES.Const.ALGORITHM);
        final XsdTypes xsdTypes = new XsdTypes(ResourceU.resolve(Irby.App.XSD));
        final TypeInstance typeHttps11 = xsdTypes.getElementType(Irby.App.QNAME.toString()).getInstance("https11");
        final NameTypeValues ntvIn = NameTypeValuesU.create("keyStorePass", new String(ksPass));
        final ValueInstance valueInstance = ValueInstance.create(typeHttps11, ntvIn);
        final TransformContext context = new TransformContext(keyApp, null);
        final ValueInstance valueInstanceX = new ValueInstanceTransform(context).transform(valueInstance);
        final char[] ksPassProtect = valueInstanceX.getNameTypeValue(
                typeHttps11.getInstance("keyStorePass")).getValueS().toCharArray();
        // validate keystore (user feedback)
        new TLSContextFactory().getKeyManager(KS_TYPE_PKCS12, ksFile, ksPass);
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
                .setParameter("keyStorePass", new String(ksPassProtect));
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
