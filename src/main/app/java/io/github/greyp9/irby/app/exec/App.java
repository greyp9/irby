package io.github.greyp9.irby.app.exec;

import io.github.greyp9.arwo.core.lang.PlatformU;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class App {

    private App() {
    }

    private int run(final String[] args) throws IOException, URISyntaxException {
        final File fileJarD = PlatformU.getCodeLocation(getClass());
        final String filenameJar = fileJarD.getName().replace("-d", "");  // filename of primary jar
        final String classpath = filenameJar + ":lib/*";  // Linux, MacOS safe
        // assemble exec command
        final List<String> command = new ArrayList<>();
        command.add("java");  // executable
        command.addAll(Arrays.asList(args));  // input args (e.g. debugger)
        command.add("-cp");  // class path
        command.add(classpath);
        command.add("io.github.greyp9.irby.app.arwo.App");  // startup class
        final String[] commandArray = command.toArray(new String[0]);
        System.out.println(Arrays.asList(commandArray));  // user feedback
        final File dir = PlatformU.getCodeFolder(getClass());
        final Runtime runtime = Runtime.getRuntime();
        /* final Process process = */
        runtime.exec(commandArray, null, dir);
        return 0;
    }

    public static void main(final String[] args) throws Exception {
        System.exit(new App().run(args));
    }
}
