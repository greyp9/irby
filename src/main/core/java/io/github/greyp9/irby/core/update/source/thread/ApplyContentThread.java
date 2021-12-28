package io.github.greyp9.irby.core.update.source.thread;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ApplyContentThread extends Thread {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final String[] cmdarray;
    private final File dir;

    public ApplyContentThread(final String[] cmdarray, final File dir) {
        super();
        this.cmdarray = cmdarray;
        this.dir = dir;
    }

    public void run() {
        try {
            logger.info(Arrays.asList(cmdarray).toString());
            Process p = Runtime.getRuntime().exec(cmdarray, null, dir);
            logger.info(p.toString());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
