package io.github.greyp9.irby.app.dispatch;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.text.line.LineU;
import io.github.greyp9.irby.app.transform.ConfigHttpsKeyStore;
import io.github.greyp9.irby.app.transform.ConfigHttpsPort;
import io.github.greyp9.irby.app.transform.ConfigHttpsTrustStore;
import io.github.greyp9.irby.app.transform.ConfigRealm;
import io.github.greyp9.irby.app.transform.ConfigSecret;

import java.io.Console;
import java.io.IOException;
import java.util.Collection;

/**
 * Provide access to alternate application startup classes (to adjust application configuration).
 */
public final class App {

    private App() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            io.github.greyp9.irby.app.arwo.App.main(args);
        } else if ("help".equals(args[0])) {
            showUsage();
        } else if ("realm".equals(args[0])) {
            ConfigRealm.main(args);
        } else if ("secret".equals(args[0])) {
            ConfigSecret.main(args);
        } else if ("https-port".equals(args[0])) {
            ConfigHttpsPort.main(args);
        } else if ("https-keystore".equals(args[0])) {
            ConfigHttpsKeyStore.main(args);
        } else if ("https-truststore".equals(args[0])) {
            ConfigHttpsTrustStore.main(args);
        } else {
            io.github.greyp9.irby.app.arwo.App.main(args);
        }
    }

    private static void showUsage() throws IOException {
        final String resourceUsage = "io/github/greyp9/irby/usage/usage.txt";
        final String text = UTF8Codec.toString(StreamU.read(ResourceU.resolve(resourceUsage)));
        final Collection<String> lines = LineU.toLines(text);
        final Console console = System.console();
        for (String line : lines) {
            console.printf(line + SystemU.eol());
        }
    }
}
