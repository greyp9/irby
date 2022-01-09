package io.github.greyp9.irby.core.servlet.cron;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.app.AppHtml;
import io.github.greyp9.arwo.core.app.AppTitle;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.date.DateX;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.glyph.UTF16;
import io.github.greyp9.arwo.core.html.Html;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpArguments;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.http.HttpResponseU;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.io.buffer.ByteBuffer;
import io.github.greyp9.arwo.core.io.command.CommandWork;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.number.NumberScale;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.resource.PathU;
import io.github.greyp9.arwo.core.resource.Pather;
import io.github.greyp9.arwo.core.submit.SubmitToken;
import io.github.greyp9.arwo.core.submit.SubmitTokenU;
import io.github.greyp9.arwo.core.table.cell.TableViewButton;
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
import io.github.greyp9.arwo.core.value.NameTypeValue;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.arwo.core.xml.DocumentU;
import io.github.greyp9.arwo.core.xpath.XPather;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.cron.job.CronJobQ;
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
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public class CronTabServlet extends javax.servlet.http.HttpServlet {
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
        final HttpResponse httpResponse = (cronService == null)
                ? HttpResponseU.to404() : getHttpResponse(request, cronService, submitID);
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
                        final String tabName = token.getObject();
                        final String jobName = token.getObject2();
                        cronService.run(new CronJobQ(tabName, jobName, new Date()));
                    }
                }
            }
        }
        ServletU.write(HttpResponseU.to302(""), response);
    }

    private HttpResponse getHttpResponse(final ByteBuffer byteBuffer) throws IOException {
        final byte[] entity = byteBuffer.getBytes();
        final NameTypeValues headers = new NameTypeValues(
                new NameTypeValue(Http.Header.CONTENT_TYPE, Http.Mime.TEXT_PLAIN_UTF8),
                new NameTypeValue(Http.Header.CONTENT_LENGTH, entity.length));
        return new HttpResponse(HttpURLConnection.HTTP_OK, headers, new ByteArrayInputStream(entity));
    }

    private HttpResponse getHttpResponse(final HttpServletRequest request,
                                         final CronService cronServiceQ, final String submitIDQ) throws IOException {
        logger.finest(cronServiceQ.toString());
        final Pather patherName = new Pather(request.getPathInfo());
        final Pather patherDate = new Pather(patherName.getRight());
        final Pather patherStream = new Pather(patherDate.getRight());
        final CommandWork commandWork = cronServiceQ.getCommands().stream()
                .filter(c -> c.getName().equals(patherName.getLeftToken()))
                .filter(c -> c.getStart().equals(DateX.fromFilename(patherDate.getLeftToken())))
                .findFirst().orElse(null);
        if (commandWork != null) {
            return ("stderr".equals(patherStream.getLeftToken()))
                    ? getHttpResponse(commandWork.getByteBufferStderr())
                    : getHttpResponse(commandWork.getByteBufferStdout());
        }

        final RowSetMetaData metaData1 = createMetaData1(cronServiceQ.getConfig().getName());
        final RowSet rowSet1 = createRowSet1(metaData1, cronServiceQ.getConfig(), submitIDQ);
        final Table table1 = new Table(rowSet1, new Sorts(), new Filters(), metaData1.getID(), metaData1.getID());
        final TableContext tableContext1 = new TableContext(
                new ViewState(null), null, submitIDQ, App.CSS.TABLE, new Bundle(), null);
        final TableView tableView1 = new TableView(table1, tableContext1);

        final RowSetMetaData metaData2 = createMetaData2("COMMANDS");
        final String basePath = PathU.toPath("", request.getContextPath(), request.getServletPath());
        final RowSet rowSet2 = createRowSet2(metaData2, basePath, cronServiceQ.getCommands());
        final Table table2 = new Table(rowSet2, new Sorts(), new Filters(), metaData2.getID(), metaData2.getID());
        final TableContext tableContext2 = new TableContext(
                new ViewState(null), null, submitIDQ, App.CSS.TABLE, new Bundle(), null);
        final TableView tableView2 = new TableView(table2, tableContext2);

        final Document html = DocumentU.toDocument(StreamU.read(ResourceU.resolve(Const.HTML)));
        final Element body = new XPather(html, null).getElement(Html.XPath.BODY);
        tableView1.addContentTo(body);
        tableView2.addContentTo(body);
        final ServletHttpRequest httpRequest = ServletU.read(request);
        new AppHtml(httpRequest).fixup(html, new AppTitle(table1.getID()));
        final byte[] entity = DocumentU.toXHtml(html);
        final NameTypeValue contentType = new NameTypeValue(Http.Header.CONTENT_TYPE, Http.Mime.TEXT_HTML_UTF8);
        final NameTypeValue contentLength = new NameTypeValue(Http.Header.CONTENT_LENGTH, entity.length);
        final NameTypeValues headers = new NameTypeValues(contentType, contentLength);
        return new HttpResponse(HttpURLConnection.HTTP_OK, headers, new ByteArrayInputStream(entity));
    }

    private RowSetMetaData createMetaData1(final String name) {
        final ColumnMetaData[] columns = new ColumnMetaData[] {
                new ColumnMetaData("tabName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("jobName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("line", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("now", Types.VARCHAR),  // i18n metadata
        };
        return new RowSetMetaData(name, columns);  // i18n metadata
    }

    private RowSet createRowSet1(
            final RowSetMetaData metaData,
            final CronConfig cronConfig,
            final String submitIDQ) {
        final Collection<CronConfigJob> jobs = cronConfig.getJobs();
        final RowSet rowSet = new RowSet(metaData, null, null);
        for (final CronConfigJob job : jobs) {
            addRow1(rowSet, cronConfig, job, submitIDQ);
        }
        return rowSet;
    }

    private void addRow1(final RowSet rowSet, final CronConfig tab,
                        final CronConfigJob job, final String submitIDQ) {
        final SubmitToken tokenNow = new SubmitToken(
                getClass().getSimpleName(), App.Action.CRON_NOW, tab.getName(), job.getName());
        final InsertRow insertRow = new InsertRow(rowSet);
        insertRow.setNextColumn(tab.getName());
        insertRow.setNextColumn(job.getName());
        insertRow.setNextColumn(job.getSchedule());
        insertRow.setNextColumn(new TableViewButton(UTF16.PLAY, submitIDQ, tokenNow.toString()));
        rowSet.add(insertRow.getRow());
    }

    private RowSetMetaData createMetaData2(final String name) {
        final ColumnMetaData[] columns = new ColumnMetaData[] {
                new ColumnMetaData("jobName", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("date", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("stdout", Types.VARCHAR),  // i18n metadata
                new ColumnMetaData("stderr", Types.VARCHAR),  // i18n metadata
        };
        return new RowSetMetaData(name, columns);  // i18n metadata
    }

    private RowSet createRowSet2(
            final RowSetMetaData metaData,
            final String basePath,
            final Collection<CommandWork> commands) {
        final RowSet rowSet = new RowSet(metaData, null, null);
        for (final CommandWork command : commands) {
            addRow2(rowSet, basePath, command);
        }
        return rowSet;
    }

    private void addRow2(final RowSet rowSet, final String basePath, final CommandWork command) {
        final String href = PathU.toPath(basePath, command.getName(), DateX.toFilename(command.getStart()));
        final InsertRow insertRow = new InsertRow(rowSet);
        insertRow.setNextColumn(command.getName());
        insertRow.setNextColumn(XsdDateU.toXSDZMillis(command.getStart()));
        insertRow.setNextColumn(new TableViewLink(NumberScale.toString(
                command.getByteBufferStdout().getLength()), null, href));
        insertRow.setNextColumn(new TableViewLink(NumberScale.toString(
                command.getByteBufferStderr().getLength()), null, PathU.toPath(href, "stderr")));
        rowSet.add(insertRow.getRow());
    }

    private static class Const {
        private static final String HTML = "io/github/greyp9/irby/html/AppServlet.html";
    }
}
