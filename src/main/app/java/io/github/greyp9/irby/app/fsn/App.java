package io.github.greyp9.irby.app.fsn;

import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.irby.core.app.Application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public final class App {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private App() {
    }

    private int run() throws IOException {
        final String className = getClass().getName();
        final String methodName = "run()";  // i18n trace
        logger.entering(className, methodName);
        final URL url = URLCodec.toURL(new File(Const.CONFIG));
        String signal = "";
        while (!signal.contains(Const.QUIT_TOKEN)) {
            signal = new Application(className).run(url);
            logger.finer(signal);
        }
        logger.exiting(className, methodName);
        return 0;
    }

    public static void main(final String[] args) throws Exception {
        System.exit(new App().run());
    }

    private static class Const {
        private static final String CONFIG = "app.xml";  // i18n internal
        private static final String QUIT_TOKEN = "QUIT";  // i18n internal
    }
}