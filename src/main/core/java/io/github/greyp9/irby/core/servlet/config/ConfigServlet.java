package io.github.greyp9.irby.core.servlet.config;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpResponseU;
import io.github.greyp9.arwo.core.http.form.MimeHeader;
import io.github.greyp9.arwo.core.http.form.MimePart;
import io.github.greyp9.arwo.core.http.form.MultipartForm;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.irby.core.app.Application;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Properties;

public class ConfigServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = 6205292576450689836L;

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final byte[] entity = StreamU.read(ResourceU.resolve(Const.HTML));
        response.setStatus(HttpURLConnection.HTTP_OK);
        response.setContentType(Http.Mime.TEXT_HTML_UTF8);
        response.getOutputStream().write(entity);
    }

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final ServletHttpRequest httpRequest = ServletU.read(request);
        final String contentType = httpRequest.getHeader(Http.Header.CONTENT_TYPE);
        if (contentType.startsWith(Http.Mime.FORM_MULTIPART)) {
            doPostFormMultipart(httpRequest);
        }
        // redirect to clean up client POST state
        ServletU.write(HttpResponseU.to302(""), response);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void doPostFormMultipart(final ServletHttpRequest httpRequest) throws IOException {
        final ByteArrayInputStream is = httpRequest.getHttpRequest().getEntity();
        final MultipartForm form = new MultipartForm(is);
        for (final Iterator<MimePart> mimePartIt = form.iterator(); mimePartIt.hasNext(); mimePartIt.getClass()) {
            final MimePart mimePart = mimePartIt.next();
            final Properties propertiesPart = new Properties();
            for (final Iterator<MimeHeader> headerIt = mimePart.iterator(); headerIt.hasNext(); headerIt.getClass()) {
                final MimeHeader mimeHeader = headerIt.next();
                mimeHeader.addTo(propertiesPart);
            }
            final String name = propertiesPart.getProperty(App.Post.CD_NAME);
            if (App.Post.UPLOAD_FILE.equals(name)) {
                final byte[] bytes = mimePart.getBody().toByteArray();
                final File file = new File(Application.Const.FILE);
                if (SystemU.isTrue()) {  // validate?
                    StreamU.write(file, bytes);
                    System.setProperty(Application.class.getName(), getClass().getName());
                }
            }
        }
    }

    private static class Const {
        private static final String HTML = "io/github/greyp9/irby/html/ConfigServlet.html";
    }
}
