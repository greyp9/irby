package io.github.greyp9.irby.app.arwo;

import io.github.greyp9.arwo.core.app.AppFolder;
import io.github.greyp9.arwo.core.app.AppSignal;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;
import io.github.greyp9.irby.app.logging.AppLogger;
import io.github.greyp9.irby.core.app.Application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class App {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private App() {
    }

    private int run() {
        final boolean mkdirData = new File(".", "data").mkdir();
        final boolean mkdirLog = new File(".", "log").mkdir();
        final String className = getClass().getName();
        final String methodName = "run()";  // i18n trace
        logger.entering(className, methodName);
        logger.finest("userDir:" + new File("").getAbsolutePath());
        logger.finest("arwoHome:" + AppFolder.getWebappRoot("").getAbsolutePath());
        logger.finest("mkdir(data):" + mkdirData);
        logger.finest("mkdir(log):" + mkdirLog);
        try {
            final URL url = URLCodec.toURL(new File(Application.Const.FILE));
            final String signal = applicationRunLoop(url);
            logger.finer(signal);
        } catch (final Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        logger.exiting(className, methodName);
        return 0;
    }

    private String applicationRunLoop(final URL url) throws IOException, GeneralSecurityException {
        String signal = "";
        while (!signal.contains(AppSignal.QUIT)) {
            signal = new Application(AppSignal.NAME).run(url);
            logger.finer(signal);
            ThreadU.sleepMillis(DurationU.Const.ONE_SECOND_MILLIS);  // no 100% CPU on error
        }
        return signal;
    }

    public static void main(final String[] args) throws Exception {
        new AppLogger().initialize();
        System.exit(new App().run());
    }
}
