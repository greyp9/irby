package io.github.greyp9.irby.core.app.config;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.NumberU;
import io.github.greyp9.arwo.core.xml.DocumentU;
import io.github.greyp9.arwo.core.xpath.XPathContext;
import io.github.greyp9.arwo.core.xpath.XPathContextFactory;
import io.github.greyp9.arwo.core.xpath.XPather;
import io.github.greyp9.irby.core.http11.config.Http11Config;
import io.github.greyp9.irby.core.http11.config.Http11ConfigContext;
import io.github.greyp9.irby.core.http11.config.Http11ConfigServlet;
import io.github.greyp9.irby.core.https11.config.Https11Config;
import io.github.greyp9.irby.core.realm.config.PrincipalConfig;
import io.github.greyp9.irby.core.realm.config.RealmConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApplicationConfig {
    private final XPathContext context;

    private final int threads;
    private final long interval;

    private final Collection<RealmConfig> realmConfigs;
    private final Collection<Http11Config> http11Configs;
    private final Collection<Https11Config> https11Configs;

    public final int getThreads() {
        return threads;
    }

    public final long getInterval() {
        return interval;
    }

    public final Collection<RealmConfig> getRealmConfigs() {
        return realmConfigs;
    }

    public final Collection<Http11Config> getHttp11Configs() {
        return http11Configs;
    }

    public final Collection<Https11Config> getHttps11Configs() {
        return https11Configs;
    }

    public ApplicationConfig(final URL url) throws IOException {
        final Document document = DocumentU.toDocument(StreamU.read(url));
        this.context = XPathContextFactory.create(document);
        final XPather xpather = new XPather(document, context);
        // application params
        this.threads = NumberU.toInt(xpather.getTextAttr("@threads"), 2);
        this.interval = NumberU.toLong(xpather.getTextAttr("@loop"), DurationU.Const.ONE_SECOND_MILLIS);
        // subsystems
        this.realmConfigs = new ArrayList<RealmConfig>();
        this.http11Configs = new ArrayList<Http11Config>();
        this.https11Configs = new ArrayList<Https11Config>();
        doElementsRealm(xpather.getElements("/irby:application/irby:realm[@enabled='true']"));
        doElementsHttp11(xpather.getElements("/irby:application/irby:http11[@enabled='true']"));
        doElementsHttps11(xpather.getElements("/irby:application/irby:https11[@enabled='true']"));
    }

    private void doElementsRealm(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            realmConfigs.add(doElementRealm(element));
        }
    }

    private RealmConfig doElementRealm(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final String method = xpather.getTextAttr("@method");
        final RealmConfig realmConfig = new RealmConfig(name, method);
        final List<Element> elements = xpather.getElements("irby:principal");
        for (final Element elementIt : elements) {
            realmConfig.addPrincipal(doElementPrincipal(elementIt));
        }
        return realmConfig;
    }

    private PrincipalConfig doElementPrincipal(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final String credential = xpather.getTextAttr("@credential");
        final String roles = xpather.getTextAttr("@roles");
        return new PrincipalConfig(name, credential, roles);
    }

    private void doElementsHttp11(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            http11Configs.add(doElementHttp11(element));
        }
    }

    private Http11Config doElementHttp11(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final int port = NumberU.toInt(xpather.getTextAttr("@port"), 0);
        final int threadsPort = NumberU.toInt(xpather.getTextAttr("@threads"), 0);
        final Http11Config http11Config = new Http11Config(name, port, threadsPort);
        final List<Element> elements = xpather.getElements("irby:web-app");
        for (final Element elementIt : elements) {
            http11Config.addContext(doElementWebapp(elementIt));
        }
        return http11Config;
    }

    private Http11ConfigContext doElementWebapp(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final String path = xpather.getTextAttr("@path");
        final String realm = xpather.getTextAttr("@realm");
        final Http11ConfigContext servletContext = new Http11ConfigContext(name, path, realm);
        final List<Element> elements = xpather.getElements("irby:servlet");
        for (final Element elementIt : elements) {
            servletContext.addServlet(doElementServlet(elementIt));
        }
        return servletContext;
    }

    private Http11ConfigServlet doElementServlet(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final String path = xpather.getTextAttr("@path");
        final String className = xpather.getTextAttr("@class");
        final String authConstraint = xpather.getTextAttr("@auth-constraint");
        final Http11ConfigServlet servletConfig = new Http11ConfigServlet(name, path, className, authConstraint);
        final List<Element> elements = xpather.getElements("irby:init-param");
        for (final Element elementIt : elements) {
            final XPather xpatherIt = XPather.create(elementIt, context);
            final String nameIt = xpatherIt.getTextAttr(Const.XPATH_A_NAME);
            final String valueIt = xpatherIt.getTextAttr("@value");
            servletConfig.addInitParam(nameIt, valueIt);
        }
        return servletConfig;
    }

    private void doElementsHttps11(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            https11Configs.add(doElementHttps11(element));
        }
    }

    private Https11Config doElementHttps11(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final int port = NumberU.toInt(xpather.getTextAttr("@port"), 0);
        final int threadsPort = NumberU.toInt(xpather.getTextAttr("@threads"), 0);
        final String keyStoreFile = xpather.getTextAttr("@keyStoreFile");
        final String keyStoreType = xpather.getTextAttr("@keyStoreType");
        final String keyStorePass = xpather.getTextAttr("@keyStorePass");
        final String trustStoreFile = xpather.getTextAttr("@trustStoreFile");
        final String trustStoreType = xpather.getTextAttr("@trustStoreType");
        final String trustStorePass = xpather.getTextAttr("@trustStorePass");
        final String protocol = xpather.getTextAttr("@protocol");
        final Https11Config https11Config = new Https11Config(name, port, threadsPort,
                keyStoreFile, keyStoreType, keyStorePass, trustStoreFile, trustStoreType, trustStorePass, protocol);
        final List<Element> elements = xpather.getElements("irby:web-app");
        for (final Element elementIt : elements) {
            https11Config.addContext(doElementWebapp(elementIt));
        }
        return https11Config;
    }

    private static class Const {
        private static final String XPATH_A_NAME = "@name";
    }
}
