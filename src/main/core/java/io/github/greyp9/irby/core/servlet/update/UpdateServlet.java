package io.github.greyp9.irby.core.servlet.update;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.app.AppFolder;
import io.github.greyp9.arwo.core.file.meta.FileMetaData;
import io.github.greyp9.arwo.core.file.meta.MetaFile;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.http.HttpResponseU;
import io.github.greyp9.arwo.core.http.form.MimeHeader;
import io.github.greyp9.arwo.core.http.form.MimePart;
import io.github.greyp9.arwo.core.http.form.MultipartForm;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.irby.core.update.source.content.ApplyContent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Properties;

public class UpdateServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = -5859395371067111934L;

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
        final boolean isMultipart = contentType.startsWith(Http.Mime.FORM_MULTIPART);
        final HttpResponse httpResponseDefault = HttpResponseU.to302("");
        final HttpResponse httpResponse = isMultipart ? doPostFormMultipart(httpRequest) : httpResponseDefault;
        // redirect to clean up client POST state
        ServletU.write(httpResponse, response);
    }

    private HttpResponse doPostFormMultipart(final ServletHttpRequest httpRequest) throws IOException {
        MetaFile metaFile = null;
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
                metaFile = toMetaFile(mimePart, propertiesPart);
            }
        }
        final HttpResponse httpResponseDefault = HttpResponseU.to302("");
        return (metaFile == null) ? httpResponseDefault : doMetaFile(httpRequest, metaFile);
    }

    private MetaFile toMetaFile(final MimePart mimePart, final Properties propertiesPart) {
        final String filename = propertiesPart.getProperty(App.Post.CD_FILENAME);
        final String contentType = propertiesPart.getProperty(Http.Header.CONTENT_TYPE);
        final byte[] bytes = mimePart.getBody().toByteArray();
        final FileMetaData metaData = new FileMetaData(filename, bytes.length, 0, false);
        return new MetaFile(metaData, contentType, new ByteArrayInputStream(bytes));
    }

    private HttpResponse doMetaFile(final ServletHttpRequest httpRequest, final MetaFile metaFile)
            throws IOException {
        // persist incoming file to filesystem
        final File folderWebappRoot = AppFolder.getWebappRoot(httpRequest.getContextPath());
        final File folderUser = AppFolder.getUserHome(folderWebappRoot, httpRequest.getPrincipal());
        final File folderUpdate = new File(folderUser, "update");
        // content incoming file
        new ApplyContent(folderUpdate).apply(metaFile);
        return HttpResponseU.to302("");
    }

    private static class Const {
        private static final String HTML = "io/github/greyp9/irby/html/ConfigServlet.html";
    }
}
