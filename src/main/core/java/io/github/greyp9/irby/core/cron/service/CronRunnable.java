package io.github.greyp9.irby.core.cron.service;

import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.vm.mutex.MutexU;
import io.github.greyp9.irby.core.cron.config.CronConfig;

import javax.naming.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class CronRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final CronService service;
    private final AtomicReference<String> reference;

    public static CronRunnable create(final CronConfig config,
                                      final ExecutorService executorService,
                                      final AtomicReference<String> reference) {
        return new CronRunnable(config, executorService, reference);
    }

    public CronRunnable(final CronConfig config,
                        final ExecutorService executorService,
                        final AtomicReference<String> reference) {
        this.service = new CronService(config, executorService, reference);
        this.reference = reference;
    }

    @Override
    public final void run() {
        final String serviceName = service.getConfig().getName();
        final String methodName = String.format("run(%s)", serviceName);
        logger.entering(getClass().getSimpleName(), methodName);
        // service
        final String subcontextName = service.getClass().getName();
        final Context subcontext = AppNaming.createSubcontext(subcontextName);
        AppNaming.bind(subcontext, serviceName, service);
        service.run();
        AppNaming.unbind(subcontext, serviceName);
        AppNaming.destroySubcontext(subcontextName);
        // service
        MutexU.notifyAll(reference);
        logger.exiting(getClass().getSimpleName(), methodName);
    }
}
