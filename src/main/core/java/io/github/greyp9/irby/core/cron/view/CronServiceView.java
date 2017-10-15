package io.github.greyp9.irby.core.cron.view;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.app.AppHtml;
import io.github.greyp9.arwo.core.app.AppTitle;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.glyph.UTF16;
import io.github.greyp9.arwo.core.html.Html;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.res.ResourceU;
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
import io.github.greyp9.arwo.core.value.NameTypeValue;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.arwo.core.xml.DocumentU;
import io.github.greyp9.arwo.core.xpath.XPather;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.cron.service.CronService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.Binding;
import javax.naming.Context;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Types;
import java.util.Collection;

public class CronServiceView {
    private final ServletHttpRequest httpRequest;
    private final Context context;
    private final String submitID;

    public CronServiceView(final ServletHttpRequest httpRequest, final Context context, final String submitID) {
        this.httpRequest = httpRequest;
        this.context = context;
        this.submitID = submitID;
    }

    public final HttpResponse doGetResponse() throws IOException {
        // template html
        final Document html = DocumentU.toDocument(StreamU.read(ResourceU.resolve(App.Html.UI)));
        final Element body = new XPather(html, null).getElement(Html.XPath.BODY);
        // context-specific content
        final String tableID = addContentTo(body);
        new AppHtml(httpRequest).fixup(html, new AppTitle(tableID));
        // package into response
        final byte[] entity = DocumentU.toXHtml(html);
        final NameTypeValue contentType = new NameTypeValue(Http.Header.CONTENT_TYPE, Http.Mime.TEXT_HTML_UTF8);
        final NameTypeValue contentLength = new NameTypeValue(Http.Header.CONTENT_LENGTH, entity.length);
        final NameTypeValues headers = new NameTypeValues(contentType, contentLength);
        return new HttpResponse(HttpURLConnection.HTTP_OK, headers, new ByteArrayInputStream(entity));
    }

    public final String addContentTo(final Element html) throws IOException {
        final RowSetMetaData metaData = createMetaData(CronService.class.getSimpleName());
        final RowSet rowSet = createRowSet(metaData);
        final Table table = new Table(rowSet, new Sorts(), new Filters(), metaData.getID(), metaData.getID());
        final TableContext tableContext = new TableContext(
                new ViewState(null), null, submitID, App.CSS.TABLE, new Bundle(), null);
        final TableView tableView = new TableView(table, tableContext);
        tableView.addContentTo(html);
        return table.getID();
    }

    private RowSetMetaData createMetaData(final String name) {
        final ColumnMetaData[] columns = new ColumnMetaData[] {
                new ColumnMetaData("cronTab", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("timezone", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("jobName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("jobType", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("line", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("now", Types.VARCHAR),  // i18n metadata
        };
        return new RowSetMetaData(name, columns);  // i18n metadata
    }

    private RowSet createRowSet(final RowSetMetaData metaData) throws IOException {
        final RowSet rowSet = new RowSet(metaData, null, null);
        final Collection<Binding> bindings = AppNaming.listBindings(context, ".*");
        for (final Binding binding : bindings) {
            final CronService cronService = (CronService) binding.getObject();
            final CronConfig cronTab = cronService.getConfig();
            final Collection<CronConfigJob> jobs = cronTab.getJobs();
            for (final CronConfigJob job : jobs) {
                addRow(rowSet, cronTab, job);
            }
        }
        return rowSet;
    }

    private void addRow(final RowSet rowSet, final CronConfig cronTab, final CronConfigJob job) throws IOException {
        final String subject = getClass().getSimpleName();
        final SubmitToken tokenNow = new SubmitToken(subject, App.Action.CRON_NOW, cronTab.getName(), job.getName());
        final InsertRow insertRow = new InsertRow(rowSet);
        insertRow.setNextColumn(cronTab.getName());
        insertRow.setNextColumn(cronTab.getTimezone());
        insertRow.setNextColumn(job.getName());
        insertRow.setNextColumn(job.getClassName());
        insertRow.setNextColumn(job.getSchedule());
        insertRow.setNextColumn(new TableViewButton(UTF16.PLAY, submitID, tokenNow.toString()));
        rowSet.add(insertRow.getRow());
    }
}
