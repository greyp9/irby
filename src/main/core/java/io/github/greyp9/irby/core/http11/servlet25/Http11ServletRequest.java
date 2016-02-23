package io.github.greyp9.irby.core.http11.servlet25;

import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.irby.core.http11.payload.Http11Request;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("PMD.ExcessivePublicCount")
public class Http11ServletRequest implements javax.servlet.http.HttpServletRequest {
    private final Http11Request request;
    private final Http11ServletConfig servletConfig;
    private final Principal principal;

    public Http11ServletRequest(
            final Http11Request request, final Http11ServletConfig servletConfig, final Principal principal) {
        this.request = request;
        this.servletConfig = servletConfig;
        this.principal = principal;
    }

    @Override
    public final String getAuthType() {
        throw new IllegalStateException();
    }

    @Override
    public final Cookie[] getCookies() {
        throw new IllegalStateException();
    }

    @Override
    public final long getDateHeader(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final String getHeader(final String s) {
        return request.getHeader().getProperties().getProperty(s);
    }

    @Override
    public final Enumeration getHeaders(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final Enumeration getHeaderNames() {
        return request.getHeader().getProperties().propertyNames();
    }

    @Override
    public final int getIntHeader(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final String getMethod() {
        return request.getHeader().getMethod();
    }

    @Override
    public final String getPathInfo() {
        final String prefix = getContextPath() + getServletPath();
        final String requestURI = request.getHeader().getRequestURI();
        if (requestURI.startsWith(prefix)) {
            return Value.defaultOnEmpty(requestURI.substring(prefix.length()), null);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public final String getPathTranslated() {
        throw new IllegalStateException();
    }

    @Override
    public final String getContextPath() {
        return servletConfig.getServletContext().getContextPath();
    }

    @Override
    public final String getQueryString() {
        return request.getHeader().getQueryString();
    }

    @Override
    public final String getRemoteUser() {
        throw new IllegalStateException();
    }

    @Override
    public final boolean isUserInRole(final String s) {
        final Http11ServletContext servletContext = (Http11ServletContext) servletConfig.getServletContext();
        return servletContext.getContext().getRealm().isUserInRole(principal, s);
    }

    @Override
    public final Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public final String getRequestedSessionId() {
        throw new IllegalStateException();
    }

    @Override
    public final String getRequestURI() {
        return request.getHeader().getRequestURI();
    }

    @Override
    public final StringBuffer getRequestURL() {
        throw new IllegalStateException();
    }

    @Override
    public final String getServletPath() {
        return servletConfig.getConfig().getPath();
    }

    @Override
    public final HttpSession getSession(final boolean b) {
        throw new IllegalStateException();
    }

    @Override
    public final HttpSession getSession() {
        throw new IllegalStateException();
    }

    @Override
    public final boolean isRequestedSessionIdValid() {
        throw new IllegalStateException();
    }

    @Override
    public final boolean isRequestedSessionIdFromCookie() {
        throw new IllegalStateException();
    }

    @Override
    public final boolean isRequestedSessionIdFromURL() {
        throw new IllegalStateException();
    }

    /**
     * @deprecated
     */
    @Override
    public final boolean isRequestedSessionIdFromUrl() {
        throw new IllegalStateException();
    }

    @Override
    public final Object getAttribute(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final Enumeration getAttributeNames() {
        throw new IllegalStateException();
    }

    @Override
    public final String getCharacterEncoding() {
        throw new IllegalStateException();
    }

    @Override
    public final void setCharacterEncoding(final String s) throws UnsupportedEncodingException {
        throw new IllegalStateException();
    }

    @Override
    public final int getContentLength() {
        throw new IllegalStateException();
    }

    @Override
    public final String getContentType() {
        throw new IllegalStateException();
    }

    @Override
    public final ServletInputStream getInputStream() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public final String getParameter(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final Enumeration getParameterNames() {
        throw new IllegalStateException();
    }

    @Override
    public final String[] getParameterValues(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final Map getParameterMap() {
        throw new IllegalStateException();
    }

    @Override
    public final String getProtocol() {
        return request.getHeader().getProtocol();
    }

    @Override
    public final String getScheme() {
        throw new IllegalStateException();
    }

    @Override
    public final String getServerName() {
        throw new IllegalStateException();
    }

    @Override
    public final int getServerPort() {
        throw new IllegalStateException();
    }

    @Override
    public final BufferedReader getReader() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public final String getRemoteAddr() {
        throw new IllegalStateException();
    }

    @Override
    public final String getRemoteHost() {
        throw new IllegalStateException();
    }

    @Override
    public final void setAttribute(final String s, final Object o) {
        throw new IllegalStateException();
    }

    @Override
    public final void removeAttribute(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final Locale getLocale() {
        throw new IllegalStateException();
    }

    @Override
    public final Enumeration getLocales() {
        throw new IllegalStateException();
    }

    @Override
    public final boolean isSecure() {
        throw new IllegalStateException();
    }

    @Override
    public final RequestDispatcher getRequestDispatcher(final String s) {
        throw new IllegalStateException();
    }

    /**
     * @deprecated
     */
    @Override
    public final String getRealPath(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final int getRemotePort() {
        return request.getSocket().getPort();
    }

    @Override
    public final String getLocalName() {
        throw new IllegalStateException();
    }

    @Override
    public final String getLocalAddr() {
        throw new IllegalStateException();
    }

    @Override
    public final int getLocalPort() {
        return request.getSocket().getLocalPort();
    }
}
