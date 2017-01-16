package io.github.greyp9.irby.core.servlet.file;

import io.github.greyp9.arwo.core.date.DateConvertU;
import io.github.greyp9.arwo.core.date.HttpDateU;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.file.FileX;
import io.github.greyp9.arwo.core.glyph.UTF16;
import io.github.greyp9.arwo.core.html.Html;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.arwo.core.value.NTV;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.arwo.core.xml.DocumentU;
import io.github.greyp9.arwo.core.xml.ElementU;
import io.github.greyp9.arwo.core.xpath.XPather;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.Principal;

public class FileServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = 2467848157165485908L;

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final String initParamFile = getInitParameter(Const.INIT_PARAM_FILE);
        final String initParamIndex = getInitParameter(Const.INIT_PARAM_INDEX);
        if (initParamFile == null) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } else {
            doGet(request, response, SystemU.resolve(initParamFile), initParamIndex);
        }
    }

    private void doGet(final HttpServletRequest request, final HttpServletResponse response,
                       final String initParamFile, final String initParamIndex)
            throws ServletException, IOException {
        final String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendRedirect(request.getRequestURI() + Http.Token.SLASH);
        } else {
            doGet2(request, response, initParamFile, initParamIndex);
        }
    }

    private void doGet2(final HttpServletRequest request, final HttpServletResponse response,
                        final String initParamFile, final String initParamIndex)
            throws ServletException, IOException {
        final File file = new File(initParamFile);
        if (file.exists()) {
            doGet(request, response, file, initParamIndex);
        } else {
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    private void doGet(final HttpServletRequest request, final HttpServletResponse response,
                       final File file, final String initParamIndex)
            throws ServletException, IOException {
        final String pathInfo = request.getPathInfo();
        final String resource = URLCodec.decode((pathInfo == null) ? "" : pathInfo);
        final boolean isDirectoryLink = resource.endsWith(Http.Token.SLASH);
        final File fileGet = ((initParamIndex == null) ? new File(file, resource) : new File(file, initParamIndex));
        final boolean exists = fileGet.exists();
        final boolean isFile = fileGet.isFile();
        final boolean isDirectory = fileGet.isDirectory();
        if (isFile) {
            doGetFile(request, response, fileGet);
        } else if (isDirectory && isDirectoryLink) {
            doGetFolder(request, response, fileGet);
        } else if (isDirectory) {
            response.setStatus(HttpURLConnection.HTTP_MOVED_TEMP);
            response.setHeader(Http.Header.LOCATION, request.getRequestURI() + Http.Token.SLASH);
        } else if (exists) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } else {
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    private void doGetFile(final HttpServletRequest request, final HttpServletResponse response, final File file)
            throws ServletException, IOException {
        request.getClass();
        final String contentType = getInitParameter(Http.Token.DOT + new FileX(file.getName()).getExtension());
        final byte[] entity = StreamU.read(file);
        response.setStatus(HttpURLConnection.HTTP_OK);
        response.setContentType(Value.defaultOnEmpty(contentType, Http.Mime.TEXT_PLAIN_UTF8));
        response.getOutputStream().write(entity);
    }

    private void doGetFolder(final HttpServletRequest request, final HttpServletResponse response, final File file)
            throws ServletException, IOException {
        final Document html = DocumentU.toDocument(StreamU.read(ResourceU.resolve(Const.HTML)));
        final Element body = new XPather(html, null).getElement(Html.XPath.BODY);
        // file listing
        final Element table = ElementU.addElement(ElementU.addElement(body, Html.DIV), Html.TABLE);
        for (final File fileIt : FileU.listFiles(file)) {
            final Element tr = ElementU.addElement(table, Html.TR);
            final boolean isDirectory = fileIt.isDirectory();
            ElementU.addElement(tr, Html.TD, isDirectory ? UTF16.ICON_FOLDER : UTF16.ICON_FILE);
            final Element td = ElementU.addElement(tr, Html.TD);
            final String href = request.getRequestURI() + fileIt.getName() + (isDirectory ? Http.Token.SLASH : "");
            ElementU.addElement(td, Html.A, fileIt.getName(), NTV.create(Html.HREF, href));
            ElementU.addElement(tr, Html.TD, HttpDateU.toHttpZMilli(DateConvertU.fromMillis(fileIt.lastModified())));
            ElementU.addElement(tr, Html.TD, fileIt.length(), NTV.create(Html.CLASS, Const.CSS_NUMBER));
        }
        // show logged on user
        final Principal principal = request.getUserPrincipal();
        ElementU.addElement(body, Html.DIV, ((principal == null) ? null : principal.getName()));
        // commit response
        final byte[] entity = DocumentU.toXHtml(html);
        response.setStatus(HttpURLConnection.HTTP_OK);
        response.setContentType(Http.Mime.TEXT_HTML_UTF8);
        response.getOutputStream().write(entity);
    }

    private static class Const {
        private static final String CSS_NUMBER = "number";  // i18n css
        private static final String HTML = "io/github/greyp9/irby/html/FileServlet.html";
        private static final String INIT_PARAM_FILE = "file";  // i18n internal
        private static final String INIT_PARAM_INDEX = "index";  // i18n internal
    }
}
