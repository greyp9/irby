package io.github.greyp9.irby.core.cron.impl.file;

import io.github.greyp9.arwo.core.date.DateX;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.file.filter.FilterFiles;
import io.github.greyp9.arwo.core.file.find.FindInFolderQuery;
import io.github.greyp9.arwo.core.file.group.FileGrouper;
import io.github.greyp9.arwo.core.file.zip.ZipAppender;
import io.github.greyp9.arwo.core.lang.NumberU;
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

    public GroupFileRunnable(final String name, final Date date, final Element element) {
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
        final String interval = ElementU.getAttribute(getElement(), Const.INTERVAL);
        final String minAge = ElementU.getAttribute(getElement(), Const.MIN_AGE);
        final String ignore = SystemU.resolve(ElementU.getAttribute(getElement(), Const.IGNORE));
        final String comment = XsdDateU.toXSDZ(getDate());
        logger.log(Level.FINEST, String.format("[%s][%s][%s]", XsdDateU.toXSDZMillis(getDate()), source, target));
        try {
            new Job(getDate(), source, target, interval, minAge, ignore, comment).execute();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String SOURCE = "source";  // i18n internal
        private static final String TARGET = "target";  // i18n internal
        private static final String INTERVAL = "interval";  // i18n internal
        private static final String MIN_AGE = "min-age";  // i18n internal
        private static final String IGNORE = "ignore";  // i18n internal
    }

    private static class Job {
        private final Logger logger = Logger.getLogger(getClass().getName());

        private final Date date;
        private final String source;
        private final String target;
        private final String interval;
        private final String minAge;
        private final int ignore;
        private final String comment;

        Job(final Date date, final String source, final String target,
            final String interval, final String minAge, final String ignore, final String comment) {
            this.date = date;
            this.source = source;
            this.target = target;
            this.interval = interval;
            this.minAge = minAge;
            this.ignore = NumberU.toInt(ignore, 0);
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
            logger.finest("FOUND=%d" + files.size());
            FilterFiles.byAgeMin(files, date, DurationU.Const.ONE_MINUTE);
            logger.finest("" + files.size());
            FilterFiles.byAgeMin(files, date, minAge);
            logger.finest("" + files.size());
            FilterFiles.byOldest(files, ignore);
            logger.finest("FOUND-OP=%d" + files.size());
            final FileGrouper fileGrouper = new FileGrouper(interval);
            for (File file : files) {
                fileGrouper.add(file);
            }
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
            final String dateNN = Value.defaultOnEmpty(DateX.toFilenameMM(dateGroup), "");
            final String filenameTarget = filePatternTarget.getName().replace("$DATE", dateNN);
            final File fileTarget = new File(folderTarget, filenameTarget);
            // target file content
            final ZipAppender appender = new ZipAppender(fileTarget);
            boolean success = appender.append(comment, group.toArray(new File[group.size()]));
            logger.info("" + success);
            if (success) {
                // double write to get ZipEntry attributes to populate
                final ZipAppender appender2 = new ZipAppender(fileTarget);
                success = appender2.append(comment, new File[0]);

                for (File file : group) {
                    success &= FileU.delete(file);
                }
            }
            logger.info("" + success);
        }
    }
}
