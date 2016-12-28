package io.github.greyp9.irby.core.servlet.res;

import io.github.greyp9.arwo.core.file.FileX;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.value.Value;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class ResourceServlet extends javax.servlet.http.HttpServlet {

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final String initParamResource = getInitParameter(Const.INIT_PARAM_RESOURCE);
        final String initParamIndex = getInitParameter(Const.INIT_PARAM_INDEX);
        if ((initParamResource == null) || (initParamIndex == null)) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } else {
            doGet(request, response, initParamResource, initParamIndex);
        }
    }

    private void doGet(final HttpServletRequest request, final HttpServletResponse response,
                       final String initParamResource, final String initParamIndex)
            throws ServletException, IOException {
        final String pathInfo = request.getPathInfo();
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
            final String contentType = getInitParameter(Http.Token.DOT + new FileX(resource).getExtension());
            final byte[] entity = StreamU.read(is);
            response.setStatus(HttpURLConnection.HTTP_OK);
            response.setContentType(Value.defaultOnEmpty(contentType, Http.Mime.TEXT_PLAIN_UTF8));
            response.getOutputStream().write(entity);
        }
    }

    private boolean isFolder(InputStream is) throws IOException {
        return (isFileSystemResource(is) || isJarResource(is));
    }

    private boolean isFileSystemResource(InputStream is) {
        return (is instanceof ByteArrayInputStream);
    }

    private boolean isJarResource(InputStream is) throws IOException {
        if (is instanceof FilterInputStream) {
            try {
                if (is.available() == 0)
                    return true;
            } catch (NullPointerException e) {
                return true;
            }
        }
        return false;
    }

    private static class Const {
        private static final String INIT_PARAM_INDEX = "index";  // i18n internal
        private static final String INIT_PARAM_RESOURCE = "resource";  // i18n internal
    }
}
