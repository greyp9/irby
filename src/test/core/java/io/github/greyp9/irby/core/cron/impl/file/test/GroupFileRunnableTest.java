package io.github.greyp9.irby.core.cron.impl.file.test;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.xml.DocumentU;
import io.github.greyp9.irby.core.cron.impl.file.GroupFileRunnable;
import junit.framework.TestCase;
import org.junit.Assert;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Date;
import java.util.logging.Logger;

public class GroupFileRunnableTest extends TestCase {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public void testGroupFile() throws Exception {
        // setup (filesystem folder for test files)
        final File tempDir = new File(SystemU.tempDir());
        logger.info(tempDir.getAbsolutePath());
        Assert.assertTrue(tempDir.exists());
        final File testDir = new File(tempDir, getClass().getSimpleName());
        FileU.ensureFolder(testDir);
        Assert.assertTrue(testDir.exists());
        // setup (test files)
        if (SystemU.isTrue()) {
            StreamU.write(toFile(testDir, 1), UTF8Codec.toBytes("123"));
            StreamU.write(toFile(testDir, 2), UTF8Codec.toBytes("234"));
            StreamU.write(toFile(testDir, 3), UTF8Codec.toBytes("345"));
            StreamU.write(toFile(testDir, 4), UTF8Codec.toBytes("123"));
            StreamU.write(toFile(testDir, 5), UTF8Codec.toBytes("234"));
            StreamU.write(toFile(testDir, 6), UTF8Codec.toBytes("345"));
        }
        // job configuration
        final String config = "<group-file " +
                "interval='P1D' source='/tmp/GroupFileRunnableTest/*.txt' " +
                "target='/tmp/GroupFileRunnableTest/$DATE.zip'/>";
        // run job
        final Element element = DocumentU.toDocument(config).getDocumentElement();
        final GroupFileRunnable runnable = new GroupFileRunnable(
                getClass().getSimpleName(), new Date(), element);
        runnable.run();
    }

    private File toFile(File folder, int ordinal) {
        return new File(folder, String.format("%s%d.txt", getClass().getSimpleName(), ordinal));
    }
}
