package io.github.greyp9.irby.core.app;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.data.persist.DataPersist;
import io.github.greyp9.arwo.core.date.DateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.envsec.EnvironmentSecret;
import io.github.greyp9.arwo.core.jce.AES;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.vm.env.EnvironmentU;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.arwo.core.vm.exec.ThreadPoolU;
import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.arwo.core.vm.props.SysPropsU;
import io.github.greyp9.arwo.core.xed.core.XedU;
import io.github.greyp9.irby.core.Irby;
import io.github.greyp9.irby.core.app.config.ApplicationConfig;
import io.github.greyp9.irby.core.context.config.ContextConfig;
import io.github.greyp9.irby.core.context.factory.ContextFactory;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.service.CronRunnable;
import io.github.greyp9.irby.core.depend.ApplicationResolver;
import io.github.greyp9.irby.core.http11.config.Http11Config;
import io.github.greyp9.irby.core.http11.server.Http11Runnable;
import io.github.greyp9.irby.core.https11.config.Https11Config;
import io.github.greyp9.irby.core.https11.server.Https11Runnable;
import io.github.greyp9.irby.core.input.InputStreamRunnable;
import io.github.greyp9.irby.core.lifecycle.LifecycleRunnable;
import io.github.greyp9.irby.core.naming.IrbyContextFactory;
import io.github.greyp9.irby.core.proxy.config.ProxyConfig;
import io.github.greyp9.irby.core.proxy.server.ProxyRunnable;
import io.github.greyp9.irby.core.proxys.config.ProxysConfig;
import io.github.greyp9.irby.core.proxys.server.ProxysRunnable;
import io.github.greyp9.irby.core.realm.Realms;
import io.github.greyp9.irby.core.tcp.config.TCPConfig;
import io.github.greyp9.irby.core.tcp.server.TCPRunnable;
import io.github.greyp9.irby.core.udp.config.UDPConfig;
import io.github.greyp9.irby.core.udp.server.UDPRunnable;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Context;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Application {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final String name;

    public Application(final String name) {
        this.name = ((name == null) ? getClass().getName() : name);
    }

    @SuppressWarnings("PMD.NPathComplexity")
    public final String run(final URL url) throws IOException, GeneralSecurityException {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, IrbyContextFactory.class.getName());
        // load config
        final ApplicationConfig config = new ApplicationConfig(url);
        // recover environment secret
        registerSecret(config.getSecret());
        // capture process environment
        final DataPersist dataPersist = new DataPersist(new File("./data"), App.Action.XML);
        dataPersist.run("env", EnvironmentU.getEnv(config.getAdvancedConfig("env").getPropertyNames()));
        dataPersist.run("props", SysPropsU.getProps(config.getAdvancedConfig("props").getPropertyNames()));
        // load missing dependencies
        new ApplicationResolver(config).resolveDependencies();
        // application setup
        final ExecutorService executorService = ExecutorServiceFactory.create(
                config.getThreads(), getClass().getSimpleName());
        final AtomicReference<String> reference = new AtomicReference<>();
        // lifecycle management
        executorService.execute(new LifecycleRunnable(name, reference, config.getInterval()));
        executorService.execute(new InputStreamRunnable(System.in, reference, config.getInterval()));
        // application credentials
        final Realms realms = new Realms(config.getRealmConfigs());
        // lookup contexts
        for (final ContextConfig contextConfig : config.getContextConfigs()) {
            ContextFactory.create(contextConfig);
        }
        //final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(getClass().getName());
        //logger.info("AFTER ContextFactory.create()" + IrbyContextU.enumerate(null));
        // web servers
        for (final Http11Config http11Config : config.getHttp11Configs()) {
            executorService.execute(Http11Runnable.create(http11Config, realms, executorService, reference));
        }
        for (final Https11Config https11Config : config.getHttps11Configs()) {
            executorService.execute(Https11Runnable.create(https11Config, realms, executorService, reference));
        }
        for (final ProxyConfig proxyConfig : config.getProxyConfigs()) {
            executorService.execute(ProxyRunnable.create(proxyConfig, executorService, reference));
        }
        for (final ProxysConfig proxysConfig : config.getProxysConfigs()) {
            executorService.execute(ProxysRunnable.create(proxysConfig, executorService, reference));
        }
        for (final TCPConfig tcpConfig : config.getTCPConfigs()) {
            executorService.execute(TCPRunnable.create(tcpConfig, reference));
        }
        for (final UDPConfig udpConfig : config.getUDPConfigs()) {
            executorService.execute(UDPRunnable.create(udpConfig, reference));
        }
        for (final CronConfig cronConfig : config.getCronConfigs()) {
            executorService.execute(CronRunnable.create(cronConfig, executorService, reference));
        }
        // record executor state (should be no queued tasks)
        final Collection<String> results = new ArrayList<>();
        results.add(ThreadPoolU.getTelemetry(executorService));
        if (!ThreadPoolU.isAvailablePool(executorService, 1)) {
            reference.set("quit: insufficient threads available in application thread pool");
        }
        // wait until shutdown signaled
        while (reference.get() == null) {
            MutexU.waitUntil(reference, DurationU.add(DateU.now(), DateU.Const.TZ_GMT, DurationU.Const.ONE_HOUR));
        }
        results.add(reference.get());
        logger.info(String.format("SIGNALED:%s", results));
        // clean application shutdown
        executorService.shutdownNow();
        try {
            final boolean terminated = executorService.awaitTermination(1, TimeUnit.SECONDS);
            results.add("Executor terminated: " + terminated);
        } catch (InterruptedException e) {
            results.add(e.getMessage());
        }
        for (final ContextConfig contextConfig : config.getContextConfigs()) {
            ContextFactory.teardown(contextConfig);
        }
        //logger.info("AFTER ContextFactory.teardown()" + IrbyContextU.enumerate(null));
        return results.toString();
    }

    private void registerSecret(final String secretPath) throws IOException, GeneralSecurityException {
        final byte[] secret = (secretPath == null) ? null : new EnvironmentSecret(secretPath, null).recover();
        // register application keystore to be used for irby application document (XedWrite)
        final KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        if (secret != null) {
            final SecretKey keyApplication = new SecretKeySpec(secret, AES.Const.ALGORITHM);
            final KeyStore.ProtectionParameter parameter =
                    new KeyStore.PasswordProtection(SystemU.userDir().toCharArray());
            keyStore.setEntry(XedU.NS_URI_XED, new KeyStore.SecretKeyEntry(keyApplication), parameter);
            keyStore.setEntry(Irby.App.URI, new KeyStore.SecretKeyEntry(keyApplication), parameter);
        }
        final Context context = AppNaming.createSubcontext(EnvironmentSecret.class.getName());
        AppNaming.bind(context, keyStore.getClass().getName(), keyStore);
    }

    public static class Const {
        public static final String FILE = "conf/app.xml";
        public static final String URL_BUILTIN = "io/github/greyp9/irby/xml/builtin/app.xml";
    }
}
