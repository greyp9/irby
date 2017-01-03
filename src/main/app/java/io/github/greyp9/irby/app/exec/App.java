package io.github.greyp9.irby.app.exec;

import io.github.greyp9.arwo.core.lang.PlatformU;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class App {

    private App() {
    }

    private int run() throws IOException, URISyntaxException {
        final String[] commandArray = {
                "java",
                "-Djava.util.logging.config.file=./logging.properties",
                "-jar",
                "irby.jar"
        };

        final File dir = PlatformU.getCodeFolder(getClass());
        final Runtime runtime = Runtime.getRuntime();
        /* final Process process = */
        runtime.exec(commandArray, null, dir);
        return 0;
    }

    public static void main(final String[] args) throws Exception {
        System.exit(new App().run());
    }
}
