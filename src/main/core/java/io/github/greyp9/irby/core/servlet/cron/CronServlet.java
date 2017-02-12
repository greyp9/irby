package io.github.greyp9.irby.core.servlet.cron;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.glyph.UTF16;
import io.github.greyp9.arwo.core.html.Html;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpArguments;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.http.HttpResponseU;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.submit.SubmitToken;
import io.github.greyp9.arwo.core.submit.SubmitTokenU;
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
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.cron.service.CronService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.Context;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Types;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

public class CronServlet extends javax.servlet.http.HttpServlet {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private transient CronService cronService;
    private transient String submitID;

    @Override
    public final void init(final ServletConfig config) throws ServletException {
        super.init(config);
        final String name = getInitParameter(CronService.class.getName());
        final Context context = (name == null) ? null : AppNaming.lookupSubcontext(CronService.class.getName());
        final Object o = (context == null) ? null : AppNaming.lookup(context, name);
        this.cronService = ((o instanceof CronService) ? (CronService) o : null);
        this.submitID = UUID.randomUUID().toString();
    }

    @Override
    public final void destroy() {
        this.submitID = null;
        this.cronService = null;
        super.destroy();
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final HttpResponse httpResponse = (cronService == null) ?
                HttpResponseU.to404() : getHttpResponse(cronService, submitID);
        ServletU.write(httpResponse, response);
    }

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        if (cronService != null) {
            final ServletHttpRequest httpRequest = ServletU.read(request);
            final byte[] entity = StreamU.read(httpRequest.getHttpRequest().getEntity());
            final NameTypeValues httpArguments = HttpArguments.toArguments(entity);
            for (final NameTypeValue httpArgument : httpArguments) {
                if (submitID.equals(httpArgument.getName())) {
                    final SubmitToken token = SubmitTokenU.fromString(httpArgument.getValueS());
                    if (token != null) {
                        final String jobName = token.getObject();
                        cronService.run(jobName, null);
                    }
                }
            }
        }
        ServletU.write(HttpResponseU.to302(""), response);
    }

    private HttpResponse getHttpResponse(CronService cronService, String submitID) throws IOException {
        logger.info(cronService.toString());
        final RowSetMetaData metaData = createMetaData(cronService.getConfig().getName());
        final RowSet rowSet = createRowSet(metaData, cronService.getConfig().getJobs(), submitID);
        final Table table = new Table(rowSet, new Sorts(), new Filters(), metaData.getID(), metaData.getID());
        final TableContext tableContext = new TableContext(
                new ViewState(null), null, submitID, App.CSS.TABLE, new Bundle(), null);
        final TableView tableView = new TableView(table, tableContext);
        final Document html = DocumentU.toDocument(StreamU.read(ResourceU.resolve(Const.HTML)));
        final Element body = new XPather(html, null).getElement(Html.XPath.BODY);
        tableView.addContentTo(body);
        final byte[] entity = DocumentU.toXHtml(html);
        final NameTypeValue contentType = new NameTypeValue(Http.Header.CONTENT_TYPE, Http.Mime.TEXT_HTML_UTF8);
        final NameTypeValue contentLength = new NameTypeValue(Http.Header.CONTENT_LENGTH, entity.length);
        final NameTypeValues headers = new NameTypeValues(contentType, contentLength);
        return new HttpResponse(HttpURLConnection.HTTP_OK, headers, new ByteArrayInputStream(entity));
    }

    private RowSetMetaData createMetaData(String name) {
        final ColumnMetaData[] columns = new ColumnMetaData[] {
                new ColumnMetaData("jobName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("line", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("now", Types.VARCHAR),  // i18n metadata
        };
        return new RowSetMetaData(name, columns);  // i18n metadata
    }

    private RowSet createRowSet(
            final RowSetMetaData metaData,
            final Collection<CronConfigJob> jobs,
            String submitID) throws IOException {
        final RowSet rowSet = new RowSet(metaData, null, null);
        for (final CronConfigJob job : jobs) {
            addRow(rowSet, job, submitID);
        }
        return rowSet;
    }

    private void addRow(final RowSet rowSet, final CronConfigJob job, String submitID) throws IOException {
        final SubmitToken tokenNow = new SubmitToken(
                getClass().getSimpleName(), App.Action.CRON_NOW, job.getName(), job.getName());
        final InsertRow insertRow = new InsertRow(rowSet);
        insertRow.setNextColumn(job.getName());
        insertRow.setNextColumn(job.getSchedule());
        insertRow.setNextColumn(new TableViewButton(UTF16.PLAY, submitID, tokenNow.toString()));
        rowSet.add(insertRow.getRow());
    }

    private static class Const {
        private static final String HTML = "io/github/greyp9/irby/html/AppServlet.html";
    }
}
