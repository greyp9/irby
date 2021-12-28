package io.github.greyp9.irby.core.proxy.socket;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.file.date.FilenameFactory;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.vm.thread.ThreadU;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class ProxyStreamRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final String name;
    private final File folder;
    private final BufferedInputStream inputStream;
    private final BufferedOutputStream outputStream;
    private final AtomicReference<String> reference;

    public ProxyStreamRunnable(final String name,
                               final File folder,
                               final InputStream inputStream,
                               final OutputStream outputStream,
                               final AtomicReference<String> reference) {
        this.name = name;
        this.folder = folder;
        this.inputStream = new BufferedInputStream(inputStream);
        this.outputStream = new BufferedOutputStream(outputStream);
        this.reference = reference;
    }

    @Override
    public final void run() {
        final String className = getClass().getSimpleName();
        final String methodName = String.format("run(%s)", name);
        try {
            logger.entering(className, methodName);
            doStream();
        } catch (IOException e) {
            logger.throwing(className, methodName, e);
            reference.compareAndSet(null, e.getMessage());
        } finally {
            logger.exiting(className, methodName);
        }
    }

    private void doStream() throws IOException {
        boolean endOfStream = false;
        boolean interrupted = false;
        Date datePersist = null;
        int ordinal = -1;
        final ByteArrayOutputStream bosPersist = new ByteArrayOutputStream();
        final ByteArrayOutputStream bosChunk = new ByteArrayOutputStream();
        while ((!interrupted) && (!endOfStream) && (reference.get() == null)) {
            final int available = inputStream.available();
            //logger.log(Level.FINEST, String.format("[%s]![%d]", name, available));
            endOfStream = readBytes(inputStream, bosChunk, Math.max(1, available));
            if (bosChunk.size() > 0) {
                final byte[] bytes = bosChunk.toByteArray();
                bosChunk.reset();
                //logger.log(Level.FINEST, "\n" + new HexRenderer(16).render(bytes, 0, bytes.length));
                logger.log(Level.FINEST, String.format("[%s]->[%d]", name, bytes.length));
                outputStream.write(bytes);
                logger.log(Level.FINEST, String.format("[%d]->[%s]", bytes.length, name));
                outputStream.flush();
                datePersist = (datePersist == null) ? new Date() : datePersist;
                bosPersist.write(bytes);
            } else {
                if ((folder != null) && (bosPersist.size() > 0)) {
                    doPersist(bosPersist.toByteArray(), datePersist, ++ordinal);
                    datePersist = null;
                    bosPersist.reset();
                }
                interrupted = ThreadU.sleepMillis(DurationU.Const.HUNDRED_MILLIS);
            }
        }
        reference.compareAndSet(null, String.format("[%s][%s]", interrupted, reference.get()));
    }

    private synchronized void doPersist(
            final byte[] bytes, final Date datePersist, final int ordinal) throws IOException {
        final String pattern = String.format("$DATE.%s.%d.txt", name, ordinal);
        final File file = FilenameFactory.getUnused(folder, pattern, datePersist);
        StreamU.write(file, bytes);
    }

    private static boolean readBytes(
            final InputStream is, final ByteArrayOutputStream os, final int available) throws IOException {
        boolean endOfStream = false;
        while ((!endOfStream) && (os.size() < available)) {
            try {
                final int data = is.read();
                if (data >= 0) {
                    os.write(data);
                } else {
                    endOfStream = true;
                }
            } catch (SocketTimeoutException e) {
                break;
            }
        }
        return endOfStream;
    }
}
