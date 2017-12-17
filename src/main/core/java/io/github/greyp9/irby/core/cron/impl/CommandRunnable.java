package io.github.greyp9.irby.core.cron.impl;

import io.github.greyp9.arwo.core.command.local.ScriptContext;
import io.github.greyp9.arwo.core.command.local.ScriptRunnable;
import io.github.greyp9.arwo.core.date.DateX;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.io.script.Script;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.link.MetaLink;
import io.github.greyp9.arwo.core.locus.Locus;
import io.github.greyp9.arwo.core.locus.LocusFactory;
import io.github.greyp9.arwo.core.result.view.ResultsContext;
import io.github.greyp9.arwo.core.xml.ElementU;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

@SuppressWarnings({ "PMD.DoNotUseThreads", "unused" })
public class CommandRunnable extends CronRunnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private ExecutorService executorService;

    public final void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    public CommandRunnable(final String name, final Date date, final Element element) {
        super(name, date, element);
    }

    @Override
    public final void run() {
        final String className = getClass().getName();
        final String date = XsdDateU.toXSDZMillis(getDate());
        final String methodName = String.format("run(%s)", date);
        logger.entering(className, methodName);
        final String command = ElementU.getAttribute(getElement(), Const.COMMAND);
        try {
            final Script script = new Script(null, getDate(), null, command);
            final File folder = new File(System.getProperty("user.dir"));
            final String filename = String.format("command.%s.%s.txt", getName(), DateX.toFilename(getDate()));
            final MetaLink metaLink = new MetaLink(new File(folder, filename), null);
            final Locus locus = new LocusFactory().create("EN", DateX.Factory.createXsdUtcMilli());
            final ResultsContext resultsContext = new ResultsContext(
                    null, null, locus, null, null, null, metaLink);
            final ScriptContext scriptContext = new ScriptContext(
                    executorService, resultsContext, DurationU.Const.TEN_MILLIS, new File(SystemU.userHome()));
            final ScriptRunnable runnable = new ScriptRunnable(script, scriptContext);
            executorService.execute(runnable);
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        logger.exiting(className, methodName);
    }

    private static class Const {
        private static final String COMMAND = "command";  // i18n internal
        private static final String USER_DIR = System.getProperty("user.dir");
    }
}
