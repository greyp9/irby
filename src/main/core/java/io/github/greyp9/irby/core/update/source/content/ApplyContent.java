package io.github.greyp9.irby.core.update.source.content;

import io.github.greyp9.arwo.core.file.meta.FileMetaData;
import io.github.greyp9.arwo.core.file.meta.MetaFile;
import io.github.greyp9.arwo.core.hash.secure.HashU;
import io.github.greyp9.arwo.core.hash.text.FingerPrint;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.jar.JarVerifier;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.irby.core.app.Application;
import io.github.greyp9.irby.core.update.source.thread.ApplyContentThread;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ApplyContent {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final File folder;

    public ApplyContent(final File folder) {
        this.folder = folder;
    }

    public void apply(final MetaFile metaFile) throws IOException {
        final byte[] bytes = StreamU.read(metaFile.getBytes());
        final FileMetaData metaData = metaFile.getMetaData();
        final File fileUpdate = new File(folder, metaData.getPath());
        StreamU.writeMkdirs(fileUpdate, bytes);
        // content incoming file
        applyMetaFile(fileUpdate, bytes);
    }

    private void applyMetaFile(final File fileUpdate, final byte[] bytes) {
        try {
            // log receipt
            logger.info(String.format("PATH=[%s], SIZE=[%d], SHA1=[%s]",
                    fileUpdate.getAbsolutePath(), bytes.length, FingerPrint.toHex(HashU.sha1(bytes))));
            // validate incoming file
            final JarVerifier verifier = new JarVerifier(getClass());
            verifier.verify(new JarFile(fileUpdate));
            // setup updater
            final String userDir = SystemU.userDir();
            String[] cmdarray = new String[] {
                    "java",
                    "-Duser.dir=" + userDir,
                    //"-Djava.util.logging.config.file=" + userDir + "/loggingU.properties",
                    "-jar",
                    fileUpdate.getAbsolutePath(),
            };
            // invoke updater
            final String userHome = SystemU.userHome();
            Runtime.getRuntime().addShutdownHook(new ApplyContentThread(cmdarray, new File(userHome)));
            // signal application (to quit)
            System.setProperty(Application.class.getName(), Value.join(
                    Http.Token.DOT, Application.Const.QUIT_TOKEN, getClass().getName()));
        } catch (IOException | GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
