package io.github.greyp9.irby.core.cron.widget;

import io.github.greyp9.arwo.core.command.local.ScriptRunnable;
import io.github.greyp9.arwo.core.table.insert.InsertRow;
import io.github.greyp9.arwo.core.table.row.RowSet;
import io.github.greyp9.arwo.core.vm.mutex.CollectionU;
import io.github.greyp9.irby.core.cron.impl.CronRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Query {@link ThreadPoolExecutor} job queue for information to render.
 */
public final class ExecutorAdaptor {

    private ExecutorAdaptor() {
    }

    public static void addContentTo(final RowSet rowSet, final ThreadPoolExecutor tpe) {
        final Collection<Runnable> queue = CollectionU.copy(new ArrayList<>(), tpe.getQueue());
        for (Runnable runnable : queue) {
            final String type = runnable.getClass().getSimpleName();
            final InsertRow insertRow = new InsertRow(rowSet);
            if (runnable instanceof CronRunnable) {
                final CronRunnable cronRunnable = (CronRunnable) runnable;
                insertRow.setNextColumn(cronRunnable.getName());
                insertRow.setNextColumn(type);
                insertRow.setNextColumn(cronRunnable.getDate());
            } else if (runnable instanceof ScriptRunnable) {
                final ScriptRunnable scriptRunnable = (ScriptRunnable) runnable;
                insertRow.setNextColumn(scriptRunnable.getScript().getID());
                insertRow.setNextColumn(type);
                insertRow.setNextColumn(scriptRunnable.getScript().getDate());
            } else {
                insertRow.setNextColumn(null);
                insertRow.setNextColumn(type);
                insertRow.setNextColumn(null);
            }
            insertRow.setNextColumn(runnable.getClass().getName());
            rowSet.add(insertRow.getRow());
        }
    }
}
