package io.github.greyp9.irby.core.update.target.folder;

import io.github.greyp9.arwo.core.io.StreamU;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class UpdateFolder {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final File folderTarget;

    public UpdateFolder(final File folderTarget) {
        this.folderTarget = folderTarget;
    }

    public void updateFrom(final File file, final String prefix) throws IOException {
        final ZipFile zipFile = new ZipFile(file);
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                final String name = entry.getName();
                if (name.startsWith(prefix)) {
                    final String path = name.substring(prefix.length());
                    final File fileTarget = new File(folderTarget, path);
                    final byte[] bytes = StreamU.read(zipFile.getInputStream(entry));
                    StreamU.write(fileTarget, bytes);
                    logger.info(fileTarget.getAbsolutePath());
                }
            }
        }
    }
}
