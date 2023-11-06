package io.github.greyp9.irby.core.tcp.socket;

import io.github.greyp9.arwo.core.file.date.FilenameFactory;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.time.Stopwatch;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;
import io.github.greyp9.irby.core.tcp.config.TCPConfig;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class TCPSocketRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final TCPConfig config;
    private final Socket socket;

    public TCPSocketRunnable(final TCPConfig config, final Socket socket) {
        this.config = config;
        this.socket = socket;
    }

    private static final long MAX_AGE_MILLIS = 15000L;
    private static final long IDLE_TIMEOUT_MILLIS = 5000L;
    private static final long IDLE_LOOP_MILLIS = 100L;

    @Override
    public final void run() {
        logger.info(String.format("BEGIN:%s", socket));
        try {
            //socket.setSoTimeout(5000);
            //socket.setSoLinger();
            run2();
        } catch (SocketException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.FINE, e.getMessage(), e);
        }
        logger.info(String.format("END:%s", socket));
    }

    private void run2() throws IOException {
        try {
            final BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final Stopwatch stopwatchIdle = new Stopwatch(null);
            final Stopwatch stopwatchMax = new Stopwatch(null);
            while (!socket.isClosed()
                    && (stopwatchIdle.elapsed() < IDLE_TIMEOUT_MILLIS)
                    && (stopwatchMax.elapsed() < MAX_AGE_MILLIS)) {
                while ((bis.available() > 0) && (stopwatchMax.elapsed() < MAX_AGE_MILLIS)) {
                    final byte[] bytes = StreamU.read(bis);
                    bos.write(bytes);
                }
                if (bos.size() > 0) {
                    doMessage(socket.getInetAddress(), bos.toByteArray());
                    stopwatchIdle.reset();
                    bos.reset();
                }
                ThreadU.sleepMillis(IDLE_LOOP_MILLIS);
            }
            if (bos.size() > 0) {
                doMessage(socket.getInetAddress(), bos.toByteArray());
            }
        } finally {
            socket.close();
        }
    }

    private void doMessage(final InetAddress address, final byte[] bytes) throws IOException {
        logger.finest(String.format("[%s][%d]", address.getHostAddress(), bytes.length));
        final File file = FilenameFactory.getUnused(config.getTarget(), new Date());
        StreamU.write(file, bytes);
    }
}
