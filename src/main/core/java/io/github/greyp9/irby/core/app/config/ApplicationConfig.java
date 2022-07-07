package io.github.greyp9.irby.core.app.config;

import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.NumberU;
import io.github.greyp9.arwo.core.xml.DocumentU;
import io.github.greyp9.arwo.core.xpath.XPathContext;
import io.github.greyp9.arwo.core.xpath.XPathContextFactory;
import io.github.greyp9.arwo.core.xpath.XPather;
import io.github.greyp9.irby.core.context.config.ContextConfig;
import io.github.greyp9.irby.core.context.config.ContextObject;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.http11.config.Http11Config;
import io.github.greyp9.irby.core.http11.config.Http11ConfigContext;
import io.github.greyp9.irby.core.http11.config.Http11ConfigServlet;
import io.github.greyp9.irby.core.https11.config.Https11Config;
import io.github.greyp9.irby.core.proxy.config.ProxyConfig;
import io.github.greyp9.irby.core.proxys.config.ProxysConfig;
import io.github.greyp9.irby.core.realm.config.PrincipalConfig;
import io.github.greyp9.irby.core.realm.config.RealmConfig;
import io.github.greyp9.irby.core.udp.config.UDPConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

// i18nf
@SuppressWarnings("PMD.TooManyMethods")
public final class ApplicationConfig {
    private final XPathContext context;

    private final String secret;
    private final int threads;
    private final long interval;

    private final Collection<RealmConfig> realmConfigs;
    private final Collection<ContextConfig> contextConfigs;
    private final Collection<Http11Config> http11Configs;
    private final Collection<Https11Config> https11Configs;
    private final Collection<ProxyConfig> proxyConfigs;
    private final Collection<ProxysConfig> proxysConfigs;
    private final Collection<UDPConfig> udpConfigs;
    private final Collection<CronConfig> cronConfigs;
    private final Collection<AdvancedConfig> advancedConfigs;

    public String getSecret() {
        return secret;
    }

    public int getThreads() {
        return threads;
    }

    public long getInterval() {
        return interval;
    }

    public Collection<RealmConfig> getRealmConfigs() {
        return realmConfigs;
    }

    public Collection<ContextConfig> getContextConfigs() {
        return contextConfigs;
    }

    public Collection<Http11Config> getHttp11Configs() {
        return http11Configs;
    }

    public Collection<Https11Config> getHttps11Configs() {
        return https11Configs;
    }

    public Collection<ProxyConfig> getProxyConfigs() {
        return proxyConfigs;
    }

    public Collection<ProxysConfig> getProxysConfigs() {
        return proxysConfigs;
    }

    public Collection<UDPConfig> getUDPConfigs() {
        return udpConfigs;
    }

    public Collection<CronConfig> getCronConfigs() {
        return cronConfigs;
    }

/*
    public final Collection<AdvancedConfig> getAdvancedConfigs() {
        return advancedConfigs;
    }
*/

    public AdvancedConfig getAdvancedConfig(final String name) {
        AdvancedConfig advancedConfig = new AdvancedConfig(name, new Properties());
        for (AdvancedConfig advancedConfigIt : advancedConfigs) {
            if (advancedConfigIt.getName().equals(name)) {
                advancedConfig = advancedConfigIt;
                break;
            }
        }
        return advancedConfig;
    }

    public ApplicationConfig(final URL url) throws IOException {
        final Document document = DocumentU.toDocument(StreamU.read(url));
        this.context = XPathContextFactory.create(document);
        final XPather xpather = new XPather(document, context);
        // application params
        this.secret = xpather.getTextAttr(Const.XPATH_A_SECRET);
        this.threads = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_THREADS), 2);
        this.interval = NumberU.toLong(xpather.getTextAttr("@loop"), DurationU.Const.ONE_SECOND_MILLIS);
        // subsystems
        this.realmConfigs = new ArrayList<>();
        this.contextConfigs = new ArrayList<>();
        this.http11Configs = new ArrayList<>();
        this.https11Configs = new ArrayList<>();
        this.proxyConfigs = new ArrayList<>();
        this.proxysConfigs = new ArrayList<>();
        this.udpConfigs = new ArrayList<>();
        this.cronConfigs = new ArrayList<>();
        this.advancedConfigs = new ArrayList<>();
        doElementsRealm(xpather.getElements("/irby:application/irby:realm[@enabled='true']"));
        doElementsContext(xpather.getElements("/irby:application/irby:context[@enabled='true']"));
        doElementsHttp11(xpather.getElements("/irby:application/irby:http11[@enabled='true']"));
        doElementsHttps11(xpather.getElements("/irby:application/irby:https11[@enabled='true']"));
        doElementsProxy(xpather.getElements("/irby:application/irby:proxy[@enabled='true']"));
        doElementsProxys(xpather.getElements("/irby:application/irby:proxys[@enabled='true']"));
        doElementsUDP(xpather.getElements("/irby:application/irby:udp[@enabled='true']"));
        doElementsCronTab(xpather.getElements("/irby:application/irby:cron[@enabled='true']"));
        doElementsAdvanced(xpather.getElements("/irby:application/irby:advanced[@enabled='true']"));
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
        final String type = xpather.getTextAttr("@type");
        final String realmContext = xpather.getTextAttr("@context");
        final RealmConfig realmConfig = new RealmConfig(name, method, type, realmContext);
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

    private void doElementsContext(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            contextConfigs.add(doElementContext(element));
        }
    }

    private ContextConfig doElementContext(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final ContextConfig contextConfig = new ContextConfig(name);
        final List<Element> elements = xpather.getElements("irby:object");
        for (final Element elementIt : elements) {
            contextConfig.addObject(doElementObject(elementIt));
        }
        return contextConfig;
    }

    private ContextObject doElementObject(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final String type = xpather.getTextAttr(Const.XPATH_A_TYPE);
        final ContextObject contextObject = new ContextObject(name, type);
        final List<Element> elements = xpather.getElements("irby:param");
        for (final Element elementIt : elements) {
            contextObject.addParameter(doElementParameter(elementIt));
        }
        return contextObject;
    }

    private String doElementParameter(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        return xpather.getTextAttr(Const.XPATH_A_VALUE);
    }

    private void doElementsHttp11(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            http11Configs.add(doElementHttp11(element));
        }
    }

    private Http11Config doElementHttp11(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final int port = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_PORT), 0);
        final int threadsPort = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_THREADS), 0);
        final Http11Config http11Config = new Http11Config(name, port, threadsPort);
        final List<Element> elements = xpather.getElements("irby:web-app[@enabled='true']");
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
        final List<Element> elementsCP = xpather.getElements("irby:context-param");
        for (final Element elementIt : elementsCP) {
            final XPather xpatherIt = XPather.create(elementIt, context);
            final String nameIt = xpatherIt.getTextAttr(Const.XPATH_A_NAME);
            final String valueIt = xpatherIt.getTextAttr(Const.XPATH_A_VALUE);
            servletContext.addContextParam(nameIt, valueIt);
        }
        final List<Element> elementsS = xpather.getElements("irby:servlet[@enabled='true']");
        for (final Element elementIt : elementsS) {
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
            final String valueIt = xpatherIt.getTextAttr(Const.XPATH_A_VALUE);
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
        final int port = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_PORT), 0);
        final int threadsPort = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_THREADS), 0);
        // the TLS key used by the server to authenticate itself to incoming connections
        final String keyStoreFile = xpather.getTextAttr("@keyStoreFile");
        final String keyStoreType = xpather.getTextAttr("@keyStoreType");
        final String keyStorePass = xpather.getTextAttr("@keyStorePass");
        // the TLS certificate(s) used by the server to authenticate incoming connections
        final String clientTrustFile = xpather.getTextAttr("@clientTrustFile");
        final String clientTrustType = xpather.getTextAttr("@clientTrustType");
        final String clientTrustPass = xpather.getTextAttr("@clientTrustPass");
        final String protocol = xpather.getTextAttr("@protocol");
        final Https11Config https11Config = new Https11Config(name, port, threadsPort,
                keyStoreFile, keyStoreType, keyStorePass, clientTrustFile, clientTrustType, clientTrustPass, protocol);
        final List<Element> elements = xpather.getElements("irby:web-app[@enabled='true']");
        for (final Element elementIt : elements) {
            https11Config.addContext(doElementWebapp(elementIt));
        }
        return https11Config;
    }

    private void doElementsProxy(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            proxyConfigs.add(doElementProxy(element));
        }
    }

    private ProxyConfig doElementProxy(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final int port = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_PORT), 0);
        final int threadsPort = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_THREADS), 0);
        final String host = xpather.getTextAttr("@host");
        final String folder = xpather.getTextAttr("@folder");
        return new ProxyConfig(name, port, threadsPort, host, folder);
    }

    private void doElementsProxys(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            proxysConfigs.add(doElementProxys(element));
        }
    }

    private ProxysConfig doElementProxys(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final int port = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_PORT), 0);
        final int threadsPort = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_THREADS), 0);
        final String host = xpather.getTextAttr("@host");
        // the TLS key used by the proxy to authenticate itself to incoming connections [@ProxysServer]
        final String keyStoreFile = xpather.getTextAttr("@keyStoreFile");
        final String keyStoreType = xpather.getTextAttr("@keyStoreType");
        final String keyStorePass = xpather.getTextAttr("@keyStorePass");
        // the TLS certificate(s) used by the proxy to authenticate incoming connections [@ProxysServer]
        final String clientTrustFile = xpather.getTextAttr("@clientTrustFile");
        final String clientTrustType = xpather.getTextAttr("@clientTrustType");
        final String clientTrustPass = xpather.getTextAttr("@clientTrustPass");
        // the TLS certificate used by the proxy to authenticate the server on outgoing connections [@ProxysServer]
        final String serverTrustFile = xpather.getTextAttr("@serverTrustFile");
        final String protocol = xpather.getTextAttr("@protocol");
        final String folder = xpather.getTextAttr("@folder");
        return new ProxysConfig(name, port, threadsPort, host, keyStoreFile, keyStoreType, keyStorePass,
                clientTrustFile, clientTrustType, clientTrustPass, serverTrustFile, protocol, folder);
    }

    private void doElementsUDP(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            udpConfigs.add(doElementUDP(element));
        }
    }

    private UDPConfig doElementUDP(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final int port = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_PORT), 0);
        final String target = xpather.getTextAttr(Const.XPATH_A_TARGET);
        final int buffer = NumberU.toInt(xpather.getTextAttr("@buffer"), 0);
        return new UDPConfig(name, port, target, buffer);
    }

    private void doElementsCronTab(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            cronConfigs.add(doElementCronTab(element));
        }
    }

    private CronConfig doElementCronTab(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final String timezone = xpather.getTextAttr("@tz");
        final int threadsJob = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_THREADS), 0);
        final int threadsStream = NumberU.toInt(xpather.getTextAttr(Const.XPATH_A_STREAMS), 0);
        final CronConfig cronConfig = new CronConfig(name, timezone, threadsJob, threadsStream);
        final List<Element> elements = xpather.getElements("irby:job[@enabled='true']");
        for (final Element elementIt : elements) {
            cronConfig.addJob(doElementCronJob(elementIt));
        }
        return cronConfig;
    }

    private CronConfigJob doElementCronJob(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final String schedule = xpather.getTextAttr("@schedule");
        //final String className = xpather.getTextAttr("@class");
        final Element typeElement = xpather.getElement("*");
        final String className = (typeElement == null ? null : typeElement.getTagName());
        return new CronConfigJob(name, schedule, className, typeElement);
    }

    private void doElementsAdvanced(final List<Element> elements) throws IOException {
        for (final Element element : elements) {
            advancedConfigs.add(doElementAdvanced(element));
        }
    }

    private AdvancedConfig doElementAdvanced(final Element element) throws IOException {
        final XPather xpather = new XPather(element, context);
        final String name = xpather.getTextAttr(Const.XPATH_A_NAME);
        final Properties properties = new Properties();
        final List<Element> elements = xpather.getElements("irby:param");
        for (final Element elementIt : elements) {
            final XPather xpatherIt = new XPather(elementIt, context);
            final String nameIt = xpatherIt.getTextAttr(Const.XPATH_A_NAME);
            final String valueIt = xpatherIt.getTextAttr(Const.XPATH_A_VALUE);
            properties.setProperty(nameIt, valueIt);
        }
        return new AdvancedConfig(name, properties);
    }

    private static class Const {
        private static final String XPATH_A_NAME = "@name";
        private static final String XPATH_A_PORT = "@port";
        private static final String XPATH_A_SECRET = "@secret";
        private static final String XPATH_A_STREAMS = "@streams";
        private static final String XPATH_A_TARGET = "@target";
        private static final String XPATH_A_THREADS = "@threads";
        private static final String XPATH_A_TYPE = "@type";
        private static final String XPATH_A_VALUE = "@value";
    }
}
