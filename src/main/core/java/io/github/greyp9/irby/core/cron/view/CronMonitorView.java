package io.github.greyp9.irby.core.cron.view;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.date.DateX;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.command.CommandWork;
import io.github.greyp9.arwo.core.locus.Locus;
import io.github.greyp9.arwo.core.number.NumberScale;
import io.github.greyp9.arwo.core.resource.PathU;
import io.github.greyp9.arwo.core.table.cell.TableViewLink;
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
import io.github.greyp9.irby.core.cron.core.CronRequest;
import io.github.greyp9.irby.core.cron.service.CronService;
import org.w3c.dom.Element;

import java.io.IOException;
import java.sql.Types;
import java.util.Collection;

/**
 * Render table with data from currently running jobs in specified {@link CronService}.
 */
public class CronMonitorView {

    public void addContentTo(final Element body, final Bundle bundle, final Locus locus,
                             final CronRequest cronRequest, final CronService cronService) throws IOException {
        final ServletHttpRequest request = cronRequest.getHttpRequest();
        final RowSetMetaData metaData = createMetaData();
        final String tabName = cronService.getConfig().getName();
        final String basePath = PathU.toPath("", request.getContextPath(), request.getServletPath(), tabName);
        final RowSet rowSet = createRowSet(metaData, basePath, tabName, cronService.getCommands());
        final Table table = new Table(rowSet, new Sorts(), new Filters(), null, null);
        final TableContext tableContext = new TableContext(
                new ViewState(null), null, cronRequest.getSubmitID(), App.CSS.TABLE, bundle, locus);
        final TableView tableView = new TableView(table, tableContext);
        tableView.addContentTo(body);
    }

    private RowSetMetaData createMetaData() {
        final ColumnMetaData[] columns = new ColumnMetaData[]{
                new ColumnMetaData("tabName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("jobName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("dateScheduled", Types.TIMESTAMP),  // i18n metadata
                new ColumnMetaData("dateStarted", Types.TIMESTAMP),  // i18n metadata
                new ColumnMetaData("pid", Types.INTEGER),  // i18n metadata
                new ColumnMetaData("stdout", Types.DATALINK),  // i18n metadata
                new ColumnMetaData("stderr", Types.DATALINK),  // i18n metadata
        };
        return new RowSetMetaData("cronActiveType", columns);  // i18n metadata
    }

    private RowSet createRowSet(
            final RowSetMetaData metaData,
            final String basePath,
            final String tabName,
            final Collection<CommandWork> commands) {
        final RowSet rowSet = new RowSet(metaData, null, null);
        for (final CommandWork command : commands) {
            addRow(rowSet, basePath, tabName, command);
        }
        return rowSet;
    }

    private void addRow(final RowSet rowSet, final String basePath, final String tabName, final CommandWork command) {
        final String hrefStdout = PathU.toPath(basePath, command.getName(), DateX.toFilename(command.getScheduled()));
        final String hrefStderr = PathU.toPath(hrefStdout, "stderr");
        final int lengthStdout = command.getByteBufferStdout().getLength();
        final int lengthStderr = command.getByteBufferStderr().getLength();
        final InsertRow insertRow = new InsertRow(rowSet);
        insertRow.setNextColumn(tabName);
        insertRow.setNextColumn(command.getName());
        insertRow.setNextColumn(command.getScheduled());
        insertRow.setNextColumn(command.getStart());
        insertRow.setNextColumn(command.getPID());
        insertRow.setNextColumn(new TableViewLink(NumberScale.toString(lengthStdout), null, PathU.toDir(hrefStdout)));
        insertRow.setNextColumn(new TableViewLink(NumberScale.toString(lengthStderr), null, PathU.toDir(hrefStderr)));
        rowSet.add(insertRow.getRow());
    }
}
