package io.github.greyp9.irby.app.transform;

import io.github.greyp9.arwo.core.codec.hex.HexCodec;
import io.github.greyp9.arwo.core.envsec.EnvironmentSecret;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.jce.AES;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.xslt.XsltX;
import io.github.greyp9.irby.core.app.Application;

import java.io.Console;
import java.io.File;
import java.net.URL;
import java.security.SecureRandom;

/**
 * Update application configuration file "conf/app.xml".  Built in XSLT is used to update configuration XML.
 */
public final class ConfigSecret {

    private ConfigSecret() {
    }

    public static void main(final String[] args) throws Exception {
        // query for configuration parameters to be updated
        final Console console = System.console();
        int i = 0;
        final String secretPath = (args.length > ++i) ? args[i] : console.readLine(PROMPT_SECRET_PATH);
        // copy environment secret to application conf folder
        final File secretFileIn = new File(secretPath);
        final File secretFileApp = new File("conf", secretFileIn.getName());
        StreamU.write(secretFileApp, StreamU.read(secretFileIn));
        console.printf("Secret specification copied successfully.%n");
        // generate environment secret; persist specification
        final byte[] secret = AES.generate().getEncoded();
        new EnvironmentSecret(secretFileApp.getPath(), new SecureRandom()).generate(secret);
        console.printf("Secret generated successfully [%s].%n", HexCodec.encode(secret));
        // transform "app.xml"
        final URL urlTransform = ResourceU.resolve(XSLT_SECRET);
        final XsltX xsltX = new XsltX(StreamU.read(urlTransform));
        xsltX.setParameter("secretFile", secretFileApp.getPath());
        final File fileXml = new File(Application.Const.FILE);
        final byte[] documentSource = StreamU.read(fileXml);
        final byte[] documentTarget = xsltX.transform(documentSource);
        StreamU.write(fileXml, documentTarget);
        console.printf("Configuration update successful.%n");
        System.exit(0);
    }

    private static final String PROMPT_SECRET_PATH = "Enter path to environment secret expression: ";
    private static final String XSLT_SECRET = "io/github/greyp9/irby/xslt/config-secret.xslt";
}
