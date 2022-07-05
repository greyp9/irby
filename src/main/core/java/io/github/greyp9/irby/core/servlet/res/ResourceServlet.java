package io.github.greyp9.irby.core.servlet.res;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.date.DateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.HttpDateU;
import io.github.greyp9.arwo.core.file.FileX;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.http.gz.HttpResponseGZipU;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.value.NTV;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.arwo.core.value.Value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;

public class ResourceServlet extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = -7121477612798528644L;

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final String initParamResource = getInitParameter(Const.INIT_PARAM_RESOURCE);
        final String initParamIndex = getInitParameter(Const.INIT_PARAM_INDEX);
        if (initParamResource == null) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } else {
            doGet(request, response, initParamResource, Value.defaultOnEmpty(initParamIndex, ""));
        }
    }

    private void doGet(final HttpServletRequest request, final HttpServletResponse response,
                       final String initParamResource, final String initParamIndex) throws IOException {
        final String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendRedirect(request.getRequestURI() + Http.Token.SLASH);
        } else {
            doGet(pathInfo, request, response, initParamResource, initParamIndex);
        }
    }

    private void doGet(final String pathInfo,
                       final HttpServletRequest request, final HttpServletResponse response,
                       final String initParamResource, final String initParamIndex) throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        String resource = Value.join("", initParamResource, pathInfo);
        InputStream is = classLoader.getResourceAsStream(resource);
        if (isFolder(is)) {
            resource = Value.join("", resource, initParamIndex);
            is = classLoader.getResourceAsStream(resource);
        }
        if (is == null) {
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        } else {
            doGet(request, response, resource, StreamU.read(is));
        }
    }

    private void doGet(final HttpServletRequest request, final HttpServletResponse response,
                       final String resource, final byte[] entity) throws IOException {
        final ServletHttpRequest httpRequest = ServletU.read(request);
        final NameTypeValues headers = NTV.create();
        final String contentType = getInitParameter(Http.Token.DOT + new FileX(resource).getExtension());
        headers.add(Http.Header.CONTENT_TYPE, Value.defaultOnEmpty(contentType, Http.Mime.TEXT_PLAIN_UTF8));
        headers.addNN(Http.Header.EXPIRES, getExpires());
        final HttpResponse httpResponse = new HttpResponse(HttpURLConnection.HTTP_OK, headers, entity);
        final HttpResponse httpResponseGZ = HttpResponseGZipU.toHttpResponseGZip(httpRequest, httpResponse);
        ServletU.write(httpResponseGZ, response);
    }

    private boolean isFolder(final InputStream is) throws IOException {
        return (isFileSystemResource(is) || isJarResource(is));
    }

    private boolean isFileSystemResource(final InputStream is) {
        return (is instanceof ByteArrayInputStream);
    }

    private boolean isJarResource(final InputStream is) throws IOException {
        if (is instanceof FilterInputStream) {
            try {
                if (is.available() == 0) {
                    return true;
                }
            } catch (NullPointerException e) {
                return true;
            }
        }
        return false;
    }

    private String getExpires() {
        String expires = null;
        final String initParamExpires = getInitParameter(Const.INIT_PARAM_EXPIRES);
        if (initParamExpires != null) {
            final Date dateExpires = DurationU.add(new Date(), DateU.Const.TZ_GMT, initParamExpires);
            expires = HttpDateU.toHttpZ(dateExpires);
        }
        return expires;
    }

    private static class Const {
        private static final String INIT_PARAM_EXPIRES = "expires";  // i18n internal
        private static final String INIT_PARAM_INDEX = "index";  // i18n internal
        private static final String INIT_PARAM_RESOURCE = "resource";  // i18n internal
    }
}
