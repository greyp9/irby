package io.github.greyp9.irby.core.http11.servlet25;

import io.github.greyp9.irby.core.http11.dispatch.Http11Context;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

public class Http11ServletContext implements ServletContext {
    private final Http11Context context;

    public final Http11Context getContext() {
        return context;
    }

    public Http11ServletContext(final Http11Context context) {
        this.context = context;
    }

    @Override
    public final ServletContext getContext(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final String getContextPath() {
        return context.getConfig().getPath();
    }

    @Override
    public final int getMajorVersion() {
        throw new IllegalStateException();
    }

    @Override
    public final int getMinorVersion() {
        throw new IllegalStateException();
    }

    @Override
    public final String getMimeType(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final Set getResourcePaths(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final URL getResource(final String s) throws MalformedURLException {
        throw new IllegalStateException();
    }

    @Override
    public final InputStream getResourceAsStream(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final RequestDispatcher getRequestDispatcher(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final RequestDispatcher getNamedDispatcher(final String s) {
        throw new IllegalStateException();
    }

    /**
     * @deprecated
     */
    @Override
    public final Servlet getServlet(final String s) throws ServletException {
        throw new IllegalStateException();
    }

    /**
     * @deprecated
     */
    @Override
    public final Enumeration getServlets() {
        throw new IllegalStateException();
    }

    /**
     * @deprecated
     */
    @Override
    public final Enumeration getServletNames() {
        throw new IllegalStateException();
    }

    @Override
    public final void log(final String s) {
        throw new IllegalStateException();
    }

    /**
     * @deprecated
     */
    @Override
    public final void log(final Exception e, final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final void log(final String s, final Throwable throwable) {
        throw new IllegalStateException();
    }

    @Override
    public final String getRealPath(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final String getServerInfo() {
        throw new IllegalStateException();
    }

    @Override
    public final String getInitParameter(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final Enumeration getInitParameterNames() {
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
    public final void setAttribute(final String s, final Object o) {
        throw new IllegalStateException();
    }

    @Override
    public final void removeAttribute(final String s) {
        throw new IllegalStateException();
    }

    @Override
    public final String getServletContextName() {
        throw new IllegalStateException();
    }
}
