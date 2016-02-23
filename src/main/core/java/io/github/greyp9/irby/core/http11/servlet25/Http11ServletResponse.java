package io.github.greyp9.irby.core.http11.servlet25;

import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.irby.core.http11.payload.Http11Response;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

@SuppressWarnings("PMD.TooManyMethods")
public class Http11ServletResponse implements javax.servlet.http.HttpServletResponse {
    private final Http11Response response;

    public Http11ServletResponse(final Http11Response response) {
        this.response = response;
    }

    @Override
    public final void addCookie(final Cookie cookie) {
        throw new IllegalStateException();
    }

    @Override
    public final boolean containsHeader(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final String encodeURL(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final String encodeRedirectURL(final String s) {
        throw new IllegalStateException();
    }

    /**
     * @deprecated
     */
    @Override
    public final String encodeUrl(final String s) {
        throw new IllegalStateException();
    }

    /**
     * @deprecated
     */
    @Override
    public final String encodeRedirectUrl(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final void sendError(final int i, final String s) throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public final void sendError(final int i) throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public final void sendRedirect(final String s) throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public final void setDateHeader(final String s, final long l) {
        throw new IllegalStateException();
    }

    @Override
    public final void addDateHeader(final String s, final long l) {
        throw new IllegalStateException();
    }

    @Override
    public final void setHeader(final String s, final String s1) {
        response.addHeader(s, s1);
    }

    @Override
    public final void addHeader(final String s, final String s1) {
        throw new IllegalStateException();
    }

    @Override
    public final void setIntHeader(final String s, final int i) {
        throw new IllegalStateException();
    }

    @Override
    public final void addIntHeader(final String s, final int i) {
        throw new IllegalStateException();
    }

    @Override
    public final void setStatus(final int i) {
        response.setStatus(i);
    }

    /**
     * @deprecated
     */
    @Override
    public final void setStatus(final int i, final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final String getCharacterEncoding() {
        throw new IllegalStateException();
    }

    @Override
    public final String getContentType() {
        throw new IllegalStateException();
    }

    @Override
    public final ServletOutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public final PrintWriter getWriter() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public final void setCharacterEncoding(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final void setContentLength(final int i) {
        response.addHeader(Http.Header.CONTENT_LENGTH, i);
    }

    @Override
    public final void setContentType(final String s) {
        response.addHeader(Http.Header.CONTENT_TYPE, s);
    }

    @Override
    public final void setBufferSize(final int i) {
        throw new IllegalStateException();
    }

    @Override
    public final int getBufferSize() {
        throw new IllegalStateException();
    }

    @Override
    public final void flushBuffer() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public final void resetBuffer() {
        throw new IllegalStateException();
    }

    @Override
    public final boolean isCommitted() {
        throw new IllegalStateException();
    }

    @Override
    public final void reset() {
        throw new IllegalStateException();
    }

    @Override
    public final void setLocale(final Locale locale) {
        throw new IllegalStateException();
    }

    @Override
    public final Locale getLocale() {
        throw new IllegalStateException();
    }
}
