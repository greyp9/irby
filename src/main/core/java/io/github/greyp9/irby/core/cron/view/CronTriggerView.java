package io.github.greyp9.irby.core.cron.view;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.glyph.UTF16;
import io.github.greyp9.arwo.core.locus.Locus;
import io.github.greyp9.arwo.core.submit.SubmitToken;
import io.github.greyp9.arwo.core.table.cell.TableViewButton;
import io.github.greyp9.arwo.core.table.filter.Filters;
import io.github.greyp9.arwo.core.table.html.TableView;
import io.github.greyp9.arwo.core.table.insert.InsertRow;
import io.github.greyp9.arwo.core.table.metadata.ColumnMetaData;
import io.github.greyp9.arwo.core.table.metadata.RowSetMetaData;
import io.github.greyp9.arwo.core.table.model.Table;
import io.github.greyp9.arwo.core.table.model.TableContext;
import io.github.greyp9.arwo.core.table.row.RowSet;
import io.github.greyp9.arwo.core.table.sort.Sorts;
import io.github.greyp9.arwo.core.table.state.ViewState;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.cron.core.CronRequest;
import io.github.greyp9.irby.core.cron.service.CronService;
import org.w3c.dom.Element;

import java.io.IOException;
import java.sql.Types;
import java.util.Collection;

/**
 * Render table with buttons to trigger invocation of jobs in specified {@link CronService}.
 */
public class CronTriggerView {

    public void addContentTo(final Element body, final Bundle bundle, final Locus locus,
                             final CronRequest request, final CronService cronService) throws IOException {
        final RowSetMetaData metaData = createMetaData();
        final RowSet rowSet = createRowSet(metaData, cronService, cronService.getConfig(), request.getSubmitID());
        final Table table = new Table(rowSet, new Sorts(), new Filters(), null, null);
        final TableContext tableContext = new TableContext(
                new ViewState(null), null, request.getSubmitID(), App.CSS.TABLE, bundle, locus);
        final TableView tableView = new TableView(table, tableContext);
        tableView.addContentTo(body);
    }

    private RowSetMetaData createMetaData() {
        final ColumnMetaData[] columns = new ColumnMetaData[]{
                new ColumnMetaData("tabName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("jobName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("jobType", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("line", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("now", Types.DATALINK),  // i18n metadata
        };
        return new RowSetMetaData("cronTabEntryType", columns);  // i18n metadata
    }

    private RowSet createRowSet(
            final RowSetMetaData metaData,
            final CronService cronService,
            final CronConfig cronConfig,
            final String submitIDQ) {
        final Collection<CronConfigJob> jobs = cronConfig.getJobs();
        final RowSet rowSet = new RowSet(metaData, null, null);
        for (final CronConfigJob job : jobs) {
            addRow(rowSet, cronService, cronConfig, job, submitIDQ);
        }
        return rowSet;
    }

    private void addRow(final RowSet rowSet, final CronService cronService, final CronConfig tab,
                        final CronConfigJob job, final String submitIDQ) {
        final SubmitToken tokenNow = new SubmitToken(
                cronService.getConfig().getName(), App.Action.CRON_NOW, tab.getName(), job.getName());
        final InsertRow insertRow = new InsertRow(rowSet);
        insertRow.setNextColumn(tab.getName());
        insertRow.setNextColumn(job.getName());
        insertRow.setNextColumn(job.getClassName());
        insertRow.setNextColumn(job.getSchedule());
        insertRow.setNextColumn(new TableViewButton(UTF16.PLAY, submitIDQ, tokenNow.toString()));
        rowSet.add(insertRow.getRow());
    }
}
