package io.github.greyp9.irby.app.update;

import io.github.greyp9.arwo.core.lang.PlatformU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.irby.core.update.target.folder.UpdateFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class App {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private App() {
    }

    private int run() {
        final String className = getClass().getName();
        final String methodName = "run()";  // i18n trace
        logger.entering(className, methodName);
        final UpdateFolder updateFolder = new UpdateFolder(new File(SystemU.userDir()));
        try {
            updateFolder.updateFrom(PlatformU.getCodeLocation(getClass()), "irby/");
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
