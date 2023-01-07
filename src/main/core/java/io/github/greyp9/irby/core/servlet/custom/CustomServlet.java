package io.github.greyp9.irby.core.servlet.custom;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.file.meta.FileMetaData;
import io.github.greyp9.arwo.core.file.meta.MetaFile;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.http.HttpResponseU;
import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.res.ResourceU;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class CustomServlet extends javax.servlet.http.HttpServlet {

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final ServletHttpRequest httpRequest = ServletU.read(request);
        final String resourceQ = httpRequest.getHttpRequest().getResource();
        final HttpResponse httpResponse;
        if (resourceQ == null) {
            httpResponse = HttpResponseU.to404();
        } else if (resourceQ.equals("/hello.txt")) {
            httpResponse = HttpResponseU.to200(getText());
        } else if (resourceQ.equals("/hello.json")) {
            httpResponse = HttpResponseU.to200(getJson());
        } else {
            httpResponse = HttpResponseU.to404();
        }
        ServletU.write(httpResponse, response);
    }

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final ServletHttpRequest httpRequest = ServletU.read(request);
        final byte[] payloadIn = StreamU.read(httpRequest.getHttpRequest().getEntity());
        Logger.getLogger(getClass().getName()).info(UTF8Codec.toString(payloadIn));
        final String resourceQ = httpRequest.getHttpRequest().getResource();
        final HttpResponse httpResponse;
        if (resourceQ == null) {
            httpResponse = HttpResponseU.to404();
        } else if (resourceQ.equals("/")) {
            httpResponse = HttpResponseU.to200(getText());
        } else {
            httpResponse = HttpResponseU.to404();
        }
        ServletU.write(httpResponse, response);
    }

    private MetaFile getText() throws IOException {
        final byte[] payload = Objects.requireNonNull(
                StreamU.read(ResourceU.resolve("io/github/greyp9/irby/core/servlet/hello.txt")));
        final FileMetaData metaData = new FileMetaData(null, payload.length, 0L, false);
        return new MetaFile(metaData, "text/plain", new ByteArrayInputStream(payload));
    }

    private MetaFile getJson() throws IOException {
        final byte[] payload = Objects.requireNonNull(
                StreamU.read(ResourceU.resolve("io/github/greyp9/irby/core/servlet/hello.json")));
        final FileMetaData metaData = new FileMetaData(null, payload.length, 0L, false);
        return new MetaFile(metaData, "application/json", new ByteArrayInputStream(payload));
    }
}
