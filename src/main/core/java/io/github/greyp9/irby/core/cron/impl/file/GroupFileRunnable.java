package io.github.greyp9.irby.core.cron.impl.file;

import io.github.greyp9.arwo.core.date.DateX;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.file.find.FindInFolderQuery;
import io.github.greyp9.arwo.core.file.group.FileGrouper;
import io.github.greyp9.arwo.core.file.zip.ZipAppender;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.arwo.core.xml.ElementU;
import io.github.greyp9.irby.core.cron.impl.CronRunnable;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({ "PMD.DoNotUseThreads", "unused" })
public class GroupFileRunnable extends CronRunnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public GroupFileRunnable(Date date, Element element) {
        super(date, element);
    }

    @Override
    public final void run() {
        final String className = getClass().getName();
        final String date = XsdDateU.toXSDZMillis(getDate());
        final String methodName = String.format("run(%s)", date);
        logger.entering(className, methodName);
        final String source = SystemU.resolve(ElementU.getAttribute(getElement(), Const.SOURCE));
        final String target = SystemU.resolve(ElementU.getAttribute(getElement(), Const.TARGET));
        final String interval = ElementU.getAttribute(getElement(), Const.INTERVAL);
        final String comment = XsdDateU.toXSDZ(getDate());
        logger.log(Level.FINEST, String.format("[%s][%s][%s]", XsdDateU.toXSDZMillis(getDate()), source, target));
        try {
            new Job(source, target, interval, comment).execute();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String SOURCE = "source";  // i18n internal
        private static final String TARGET = "target";  // i18n internal
        private static final String INTERVAL = "interval";  // i18n internal
    }

    private static class Job {
        private final Logger logger = Logger.getLogger(getClass().getName());

        private final String source;
        private final String target;
        private final String interval;
        private final String comment;

        public Job(final String source, final String target, final String interval, final String comment) {
            this.source = source;
            this.target = target;
            this.interval = interval;
            this.comment = comment;
        }

        public void execute() throws IOException {
            execute(new File(source));
        }

        private void execute(final File filePatternSource) throws IOException {
            final File folderSource = filePatternSource.getParentFile();
            final String filenameSource = filePatternSource.getName();
            final FindInFolderQuery query = new FindInFolderQuery(folderSource, filenameSource, false);
            final Collection<File> files = query.getFound();
            logger.finest("" + files.size());
            final FileGrouper fileGrouper = new FileGrouper(interval);
            fileGrouper.add(files.toArray(new File[files.size()]));
            final Map<Date, Collection<File>> groupings = fileGrouper.getGroupings();
            for (final Map.Entry<Date, Collection<File>> entry : groupings.entrySet()) {
                final Date dateGroup = entry.getKey();
                final Collection<File> group = entry.getValue();
                executeGrouping(dateGroup, group);
            }
        }

        private void executeGrouping(final Date dateGroup, final Collection<File> group) throws IOException {
            final File filePatternTarget = new File(target);
            final File folderTarget = filePatternTarget.getParentFile();
            // target filename timestamp
            final String date = DateX.toFilenameMM(dateGroup);
            final String dateNN = Value.defaultOnEmpty(date, "");
            final String filenameTarget = filePatternTarget.getName().replace("$DATE", dateNN);
            final File fileTarget = new File(folderTarget, filenameTarget);
            // target file content
            final ZipAppender appender = new ZipAppender(fileTarget);
            boolean success = appender.append(comment, group.toArray(new File[group.size()]));
            logger.info("" + success);
            if (success) {
                for (File file : group) {
                    success &= FileU.delete(file);
                }
            }
            logger.info("" + success);
        }
    }
}
