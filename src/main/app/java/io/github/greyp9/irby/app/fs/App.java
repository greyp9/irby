package io.github.greyp9.irby.app.fs;

import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.irby.core.app.Application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

public final class App {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private App() {
    }

    private int run() throws IOException, GeneralSecurityException {
        final String className = getClass().getName();
        final String methodName = "run()";  // i18n trace
        logger.entering(className, methodName);
        final URL url = URLCodec.toURL(new File(Application.Const.FILE));
        final String signal = new Application(null).run(url);
        logger.finer(signal);
        logger.exiting(className, methodName);
        return 0;
    }

    public static void main(final String[] args) throws Exception {
        System.exit(new App().run());
    }
}
