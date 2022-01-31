package io.github.greyp9.irby.core.cron.view;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.glyph.UTF16;
import io.github.greyp9.arwo.core.locus.Locus;
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
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.core.CronRequest;
import io.github.greyp9.irby.core.cron.service.CronService;
import org.w3c.dom.Element;

import java.io.IOException;
import java.sql.Types;
import java.util.Collection;

/**
 * Provide UI to enumerate available {@link CronService} objects, with UI to select one (in order to operate on it).
 */
public class CronServicesView {

    public final void addContent(final Element body, final Bundle bundle, final Locus locus, final CronRequest request,
                                 final Collection<CronService> cronServices) throws IOException {
        final RowSetMetaData metaData = createMetaData();
        final RowSet rowSet = createRowSet(metaData, cronServices, request.getHttpRequest().getBaseURI());
        final Table table = new Table(rowSet, new Sorts(), new Filters(), null, null);
        final TableContext tableContext = new TableContext(
                new ViewState(null), null, request.getSubmitID(), App.CSS.TABLE, bundle, locus);
        final TableView tableView = new TableView(table, tableContext);
        tableView.addContentTo(body);
    }

    private RowSetMetaData createMetaData() {
        final ColumnMetaData[] columns = new ColumnMetaData[]{
                new ColumnMetaData("select", Types.DATALINK),  // i18n metadata
                new ColumnMetaData("tabName", Types.VARCHAR),  // i18n metadata
        };
        return new RowSetMetaData("cronTabType", columns);  // i18n metadata
    }

    private RowSet createRowSet(
            final RowSetMetaData metaData,
            final Collection<CronService> cronTabsQ, final String baseUri) {
        final RowSet rowSet = new RowSet(metaData, null, null);
        for (CronService cronTab : cronTabsQ) {
            addRow(rowSet, cronTab, baseUri);
        }
        return rowSet;
    }

    private void addRow(final RowSet rowSet, final CronService cronService, final String baseURI) {
        final CronConfig cronConfig = cronService.getConfig();
        final String href = PathU.toDir(baseURI, cronConfig.getName());
        final InsertRow insertRow = new InsertRow(rowSet);
        insertRow.setNextColumn(new TableViewLink(UTF16.SELECT, null, href));
        insertRow.setNextColumn(cronConfig.getName());
        rowSet.add(insertRow.getRow());
    }
}
