package io.github.greyp9.irby.app.arwo;

import io.github.greyp9.arwo.core.app.AppFolder;
import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.irby.app.logging.AppLogger;
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
        logger.finest("userDir:" + new File("").getAbsolutePath());
        logger.finest("arwoHome:" + AppFolder.getWebappRoot("").getAbsolutePath());
        logger.finest("mkdir(data):" + new File(".", "data").mkdir());
        logger.finest("mkdir(log):" + new File(".", "log").mkdir());
        final URL url = URLCodec.toURL(new File(Application.Const.FILE));
        final String signal = applicationRunLoop(url);
        logger.finer(signal);
        logger.exiting(className, methodName);
        return 0;
    }

    private String applicationRunLoop(final URL url) throws IOException {
        String signal = "";
        while (!signal.contains(Application.Const.QUIT_TOKEN)) {
            signal = new Application(null).run(url);
            logger.finer(signal);
        }
        return signal;
    }

    public static void main(final String[] args) throws Exception {
        new AppLogger().initialize();
        System.exit(new App().run());
    }
}
