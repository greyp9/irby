package io.github.greyp9.irby.app.update;

import io.github.greyp9.arwo.core.date.DateX;
import io.github.greyp9.arwo.core.lang.PlatformU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.irby.core.update.target.folder.UpdateFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class App {

    private App() {
    }

    private int run() {
        final Logger logger = Logger.getLogger(getClass().getName());
        final String className = getClass().getName();
        final String methodName = "run()";  // i18n trace
        try {
            // setup logging
            final String filename = String.format("update.%s.log", DateX.toFilename(new Date()));
            final File file = new File(SystemU.userDir(), filename);
            Logger.getLogger("").addHandler(new FileHandler(file.getAbsolutePath()));
            Logger.getLogger("").setLevel(Level.FINEST);
            // carry out content update
            logger.entering(className, methodName);
            final File codeLocation = PlatformU.getCodeLocation(getClass());
            logger.info(codeLocation.getAbsolutePath());
            final UpdateFolder updateFolder = new UpdateFolder(new File(SystemU.userDir()));
            updateFolder.updateFrom(codeLocation, "irby/");
        } catch (IOException e) {
            logger.throwing(className, methodName, e);
        } catch (URISyntaxException e) {
            logger.throwing(className, methodName, e);
        }
        logger.exiting(className, methodName);
        return 0;
    }

    public static void main(final String[] args) throws Exception {
        System.exit(new App().run());
    }
}
