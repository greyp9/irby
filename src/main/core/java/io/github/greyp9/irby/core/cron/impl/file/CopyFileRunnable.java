package io.github.greyp9.irby.core.cron.impl.file;

import io.github.greyp9.arwo.core.date.DateX;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.file.find.FindInFolderQuery;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.arwo.core.xml.ElementU;
import io.github.greyp9.irby.core.cron.impl.CronRunnable;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@SuppressWarnings({ "PMD.DoNotUseThreads", "unused" })
public class CopyFileRunnable extends CronRunnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public CopyFileRunnable(final String name, final Date date, final Element element) {
        super(name, date, element);
    }

    @Override
    public final void run() {
        final String className = getClass().getName();
        final String date = XsdDateU.toXSDZMillis(getDate());
        final String methodName = String.format("run(%s)", date);
        logger.entering(className, methodName);
        final String source = SystemU.resolve(ElementU.getAttribute(getElement(), Const.SOURCE));
        final String target = SystemU.resolve(ElementU.getAttribute(getElement(), Const.TARGET));
        final String move = SystemU.resolve(ElementU.getAttribute(getElement(), Const.MOVE));
        logger.log(Level.FINEST, String.format("[%s][%s][%s][%s]",
                XsdDateU.toXSDZMillis(getDate()), source, target, move));
        new Job(source, target, Boolean.parseBoolean(move)).execute();
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String SOURCE = "source";  // i18n internal
        private static final String TARGET = "target";  // i18n internal
        private static final String MOVE = "move";  // i18n internal
    }

    private static class Job {
        private final Logger logger = Logger.getLogger(getClass().getName());

        private final String source;
        private final String target;
        private final boolean move;

        Job(final String source, final String target, final boolean move) {
            this.source = source;
            this.target = target;
            this.move = move;
        }

        public void execute() {
            execute(new File(source));
        }

        private void execute(final File filePatternSource) {
            final File folderSource = filePatternSource.getParentFile();
            final Pattern pattern = Pattern.compile(filePatternSource.getName());
            final FindInFolderQuery query = new FindInFolderQuery(folderSource, pattern, false);
            final Collection<File> files = query.getFound();
            logger.finest("FOUND=%d" + files.size());
            for (File file : files) {
                executeOne(file);
            }
        }

        private void executeOne(final File fileSource) {
            final File filePatternTarget = new File(target);
            final File folderTarget = filePatternTarget.getParentFile();
            // target filename derived from source filename
            final String filenameTarget = filePatternTarget.getName().replace("*", fileSource.getName());
            // target filename timestamp
            final String date = DateX.toFilename(new Date(fileSource.lastModified()));
            final String dateNN = Value.defaultOnEmpty(date, "");
            final String filenameTarget2 = filenameTarget.replace("$DATE", dateNN);
            final File fileTarget = new File(folderTarget, filenameTarget2);
            if (!fileTarget.exists()) {
                executeOne(fileSource, fileTarget);
            }
        }

        private void executeOne(final File fileSource, final File fileTarget) {
            try {
                final byte[] bytes = StreamU.read(fileSource);
                logger.finest(String.format("%d [%s]", bytes.length, fileSource.getAbsolutePath()));
                StreamU.write(fileTarget, bytes);
                logger.finest(String.format("%d [%s]", bytes.length, fileTarget.getAbsolutePath()));
                if (move) {
                    logger.finest(String.format("%s [%s]", FileU.delete(fileSource), fileSource.getAbsolutePath()));
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
    }
}
