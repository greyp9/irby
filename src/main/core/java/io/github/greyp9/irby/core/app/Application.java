package io.github.greyp9.irby.core.app;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.date.DateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.hash.text.FingerPrint;
import io.github.greyp9.arwo.core.vm.env.EnvironmentU;
import io.github.greyp9.arwo.core.vm.exec.ExecutorServiceFactory;
import io.github.greyp9.arwo.core.vm.exec.ThreadPoolU;
import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.arwo.core.vm.props.SysPropsU;
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
import io.github.greyp9.irby.core.udp.config.UDPConfig;
import io.github.greyp9.irby.core.udp.server.UDPRunnable;

import javax.naming.Context;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Application {
    private final String name;

    public Application(final String name) {
        this.name = ((name == null) ? getClass().getName() : name);
    }

    @SuppressWarnings("PMD.NPathComplexity")
    public final String run(final URL url) throws IOException {
        // capture procenv
        final byte[] sysenv = EnvironmentU.getEnv(Collections.<String>emptyList());
        final byte[] sysprops = SysPropsU.getProps(Collections.<String>emptyList());
        final Logger logger = Logger.getLogger(getClass().getName());
        logger.finest(String.format("ENV\n%s\n%s", UTF8Codec.toString(sysenv), FingerPrint.toHex256(sysenv)));
        logger.finest(String.format("PROP\n%s\n%s", UTF8Codec.toString(sysprops), FingerPrint.toHex256(sysprops)));
        // load config
        final ApplicationConfig config = new ApplicationConfig(url);
        new ApplicationResolver(config).resolveDependencies();
        final ExecutorService executorService = ExecutorServiceFactory.create(
                config.getThreads(), getClass().getSimpleName());
        final AtomicReference<String> reference = new AtomicReference<String>();
        // lifecycle management
        executorService.execute(new LifecycleRunnable(name, reference, config.getInterval()));
        executorService.execute(new InputStreamRunnable(System.in, reference, config.getInterval()));
        // application credentials
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, IrbyContextFactory.class.getName());
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
        for (final UDPConfig udpConfig : config.getUDPConfigs()) {
            executorService.execute(UDPRunnable.create(udpConfig, reference));
        }
        for (final CronConfig cronConfig : config.getCronConfigs()) {
            executorService.execute(CronRunnable.create(cronConfig, executorService, reference));
        }
        // record executor state (should be no queued tasks)
        final Collection<String> results = new ArrayList<String>();
        results.add(ThreadPoolU.getTelemetry(executorService));
        // wait until shutdown signaled
        while (reference.get() == null) {
            MutexU.waitUntil(reference, DurationU.add(DateU.now(), DateU.Const.TZ_GMT, DurationU.Const.ONE_HOUR));
        }
        results.add(reference.get());
        // clean application shutdown
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            results.add(e.getMessage());
        }
        for (final ContextConfig contextConfig : config.getContextConfigs()) {
            ContextFactory.teardown(contextConfig);
        }
        //logger.info("AFTER ContextFactory.teardown()" + IrbyContextU.enumerate(null));
        return results.toString();
    }

    public static class Const {
        public static final String FILE = "app.xml";
        public static final String URL_BUILTIN = "io/github/greyp9/irby/xml/builtin/app.xml";

        public static final String QUIT_TOKEN = "q";  // i18n internal
    }
}
