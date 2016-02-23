package io.github.greyp9.irby.app.builtin;

import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.irby.core.app.Application;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public final class App {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private App() {
    }

    private int run() throws IOException {
        final String className = getClass().getName();
        final String methodName = "run()";
        logger.entering(className, methodName);
        final URL url = ResourceU.resolve(Const.CONFIG);
        final String signal = new Application(className).run(url);
        logger.finer(signal);
        logger.exiting(className, methodName);
        return 0;
    }

    public static void main(final String[] args) throws Exception {
        System.exit(new App().run());
    }

    private static class Const {
        private static final String CONFIG = "io/github/greyp9/irby/xml/builtin/app.xml";
    }
}