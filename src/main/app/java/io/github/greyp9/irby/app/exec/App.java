package io.github.greyp9.irby.app.exec;

import io.github.greyp9.arwo.core.lang.PlatformU;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public final class App {

    private App() {
    }

    private int run() throws IOException, URISyntaxException {
        final File fileJarD = PlatformU.getCodeLocation(getClass());
        final String filenameJar = fileJarD.getName().replace("-d", "");  // filename of primary jar
        final String classpath = filenameJar + ":lib/*";  // Linux, MacOS safe
        final String[] commandArray = {
                "java",
                "-cp",
                classpath,
                "io.github.greyp9.irby.app.arwo.App"
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
