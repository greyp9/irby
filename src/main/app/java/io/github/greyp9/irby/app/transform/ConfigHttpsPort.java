package io.github.greyp9.irby.app.transform;

import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.xslt.XsltX;
import io.github.greyp9.irby.core.app.Application;

import java.io.Console;
import java.io.File;
import java.net.URL;

/**
 * Update application configuration file "conf/app.xml" with externally supplied parameters (port number).  Built in
 * XSLT is used to update configuration XML.
 */
public final class ConfigHttpsPort {

    private ConfigHttpsPort() {
    }

    public static void main(final String[] args) throws Exception {
        // query for configuration parameters to be updated
        final Console console = System.console();
        int i = 0;
        final String port = (args.length > ++i) ? args[i] : console.readLine(PROMPT_PORT);
        // user feedback
        console.printf("port = %s.%n", port);
        // transform "app.xml"
        final URL urlTransform = ResourceU.resolve(XSLT_HTTPS);
        final XsltX xsltX = new XsltX(StreamU.read(urlTransform));
        xsltX.setParameter("port", port);
        final File fileXml = new File(Application.Const.FILE);
        final byte[] documentSource = StreamU.read(fileXml);
        final byte[] documentTarget = xsltX.transform(documentSource);
        StreamU.write(fileXml, documentTarget);
        console.printf("Configuration update successful.%n");
        System.exit(0);
    }

    private static final String PROMPT_PORT = "Enter port number: ";
    private static final String XSLT_HTTPS = "io/github/greyp9/irby/xslt/config-https.xslt";
}
