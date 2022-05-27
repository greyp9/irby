package io.github.greyp9.irby.core.servlet.cron;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.app.AppHtml;
import io.github.greyp9.arwo.core.app.AppTitle;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.date.DateX;
import io.github.greyp9.arwo.core.html.Html;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpArguments;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.http.HttpResponseU;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.io.buffer.ByteBuffer;
import io.github.greyp9.arwo.core.io.command.CommandWork;
import io.github.greyp9.arwo.core.locus.Locus;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.resource.PathU;
import io.github.greyp9.arwo.core.submit.SubmitToken;
import io.github.greyp9.arwo.core.submit.SubmitTokenU;
import io.github.greyp9.arwo.core.value.NameTypeValue;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.arwo.core.xml.DocumentU;
import io.github.greyp9.arwo.core.xpath.XPather;
import io.github.greyp9.irby.core.cron.core.CronRequest;
import io.github.greyp9.irby.core.cron.job.CronJobQ;
import io.github.greyp9.irby.core.cron.service.CronService;
import io.github.greyp9.irby.core.cron.view.CronMonitorView;
import io.github.greyp9.irby.core.cron.view.CronQueueView;
import io.github.greyp9.irby.core.cron.view.CronServicesView;
import io.github.greyp9.irby.core.cron.view.CronStandbyView;
import io.github.greyp9.irby.core.cron.view.CronTriggerView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.Binding;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CronTabServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = 7431737000728624091L;

    private final transient Logger logger = Logger.getLogger(getClass().getName());

    private transient Collection<CronService> cronServices;
    private transient String submitID;

    @Override
    public final void init(final ServletConfig config) throws ServletException {
        super.init(config);
        logger.entering(getClass().getName(), null);
        this.cronServices = AppNaming.listBindings(
                        AppNaming.lookupSubcontext(CronService.class.getName()), ".*")
                .stream().map(Binding::getObject)
                .filter(CronService.class::isInstance)
                .map(CronService.class::cast)
                .collect(Collectors.toList());
        this.submitID = UUID.randomUUID().toString();
    }

    @Override
    public final void destroy() {
        this.submitID = null;
        this.cronServices.clear();
        logger.exiting(getClass().getName(), null);
        super.destroy();
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final ServletHttpRequest servletHttpRequest = ServletU.read(request);
        final CronRequest cronRequest = new CronRequest(servletHttpRequest, submitID);
        final HttpResponse httpResponse = (servletHttpRequest.getPathInfo() == null)
                ? HttpResponseU.to302(servletHttpRequest.getHttpRequest().getResource() + Http.Token.SLASH)
                : getHttpResponse(cronRequest);
        ServletU.write(httpResponse, response);
    }

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final ServletHttpRequest servletHttpRequest = ServletU.read(request);
        final CronRequest cronRequest = new CronRequest(servletHttpRequest, submitID);
        final String cronTab = cronRequest.getCronTab();
        final CronService cronService = cronServices.stream()
                .filter(s -> s.getConfig().getName().equals(cronTab)).findFirst().orElse(null);
        if (cronService != null) {
            final byte[] entity = StreamU.read(servletHttpRequest.getHttpRequest().getEntity());
            final NameTypeValues httpArguments = HttpArguments.toArguments(entity);
            for (final NameTypeValue httpArgument : httpArguments) {
                if (submitID.equals(httpArgument.getName())) {
                    final SubmitToken token = SubmitTokenU.fromString(httpArgument.getValueS());
                    if (token != null) {
                        final String subject = token.getSubject();
                        final String action = token.getAction();
                        if ((subject.equals(cronTab)) && (action.equals(App.Action.CRON_NOW))) {
                            cronService.run(new CronJobQ(token.getObject(), token.getObject2(), new Date()));
                        } else if ((subject.equals(App.Target.USER_STATE)) && (action.equals(App.Action.UPDATE))) {
                            cronService.setStandby(httpArguments.getValue("standby.standbyType.duration"));
                        }
                    }
                }
            }
        }
        ServletU.write(HttpResponseU.to302(""), response);
    }

    private HttpResponse getHttpResponse(final CronRequest cronRequest) throws IOException {
        final String cronTab = cronRequest.getCronTab();
        final CronService cronService = cronServices.stream()
                .filter(s -> s.getConfig().getName().equals(cronTab)).findFirst().orElse(null);
        final HttpResponse httpResponse;
        if (Value.isEmpty(cronTab)) {
            httpResponse = getHttpResponse2(cronRequest, null, null);
        } else if (cronService == null) {
            httpResponse = HttpResponseU.to302(cronRequest.getHttpRequest().getBaseURI());
        } else {
            httpResponse = getHttpResponse2(cronRequest, cronTab, cronService);
        }
        return httpResponse;
    }

    private HttpResponse getHttpResponse2(final CronRequest cronRequest, final String label,
                                          final CronService cronService) throws IOException {
        final ServletHttpRequest request = cronRequest.getHttpRequest();
        final String name = cronRequest.getCronJob();
        final String date = cronRequest.getJobDate();
        final String stream = cronRequest.getJobStream();
        logger.finest(String.format("[%s][%s][%s]", name, date, stream));
        if (!Value.isEmpty(name) && !Value.isEmpty(date)) {
            final CommandWork commandWork = cronService.getCommands().stream()
                    .filter(c -> c.getName().equals(name))
                    .filter(c -> c.getScheduled().equals(DateX.fromFilename(date)))
                    .findFirst().orElse(null);
            return (commandWork == null)
                    ? HttpResponseU.to302(PathU.toDir(request.getBaseURI(), cronRequest.getCronTab()))
                    : (getHttpResponse(Const.STDERR.equals(stream)
                    ? commandWork.getByteBufferStderr()
                    : commandWork.getByteBufferStdout()));
        } else {
            return getHttpResponse3(cronRequest, label, cronService);
        }
    }

    private HttpResponse getHttpResponse(final ByteBuffer byteBuffer) throws IOException {
        final byte[] entity = byteBuffer.getBytes();
        final NameTypeValues headers = new NameTypeValues(
                new NameTypeValue(Http.Header.CONTENT_TYPE, Http.Mime.TEXT_PLAIN_UTF8),
                new NameTypeValue(Http.Header.CONTENT_LENGTH, entity.length));
        return new HttpResponse(HttpURLConnection.HTTP_OK, headers, new ByteArrayInputStream(entity));
    }

    private HttpResponse getHttpResponse3(final CronRequest cronRequest, final String label,
                                          final CronService cronService) throws IOException {
        // serve cron tab status page
        final Locale locale = Locale.getDefault();
        final Bundle bundle = new Bundle(ResourceBundle.getBundle("io.github.greyp9.irby.core.core", locale));
        final Locus locus = new Locus(locale, DateX.Factory.createXsdUtcMilli());

        final Document html = DocumentU.toDocument(StreamU.read(ResourceU.resolve(Const.HTML)));
        //new AppRefreshView(new Properties()).addContentTo(html.getDocumentElement());
        final Element body = new XPather(html, null).getElement(Html.XPath.BODY);

        if (cronService == null) {
            new CronServicesView().addContent(body, bundle, locus, cronRequest, cronServices);
        } else {
            new CronStandbyView().addContentTo(body, bundle, locus, cronRequest, cronService);
            new CronTriggerView().addContentTo(body, bundle, locus, cronRequest, cronService);
            new CronMonitorView().addContentTo(body, bundle, locus, cronRequest, cronService);
            new CronQueueView().addContentTo(body, bundle, locus, cronRequest, cronService);
        }
        new AppHtml(cronRequest.getHttpRequest()).fixup(html, new AppTitle(Value.join(" - ", label, "CronTab")));
        final byte[] entity = DocumentU.toXHtml(html);
        final NameTypeValue contentType = new NameTypeValue(Http.Header.CONTENT_TYPE, Http.Mime.TEXT_HTML_UTF8);
        final NameTypeValue contentLength = new NameTypeValue(Http.Header.CONTENT_LENGTH, entity.length);
        final NameTypeValues headers = new NameTypeValues(contentType, contentLength);
        return new HttpResponse(HttpURLConnection.HTTP_OK, headers, new ByteArrayInputStream(entity));
    }

    private static class Const {
        private static final String HTML = "io/github/greyp9/irby/html/AppServlet.html";
        private static final String STDERR = "stderr";
    }
}
