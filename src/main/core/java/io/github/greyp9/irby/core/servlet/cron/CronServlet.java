package io.github.greyp9.irby.core.servlet.cron;

import io.github.greyp9.arwo.app.core.servlet.ServletU;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.http.HttpResponseU;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.irby.core.cron.config.CronConfig;
import io.github.greyp9.irby.core.cron.config.CronConfigJob;
import io.github.greyp9.irby.core.cron.service.CronService;

import javax.naming.Context;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

public class CronServlet extends javax.servlet.http.HttpServlet {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final HttpResponse httpResponse = HttpResponseU.to500(getClass().getName());
        final String name = getInitParameter(CronService.class.getName());
        final Context context = (name == null) ? null : AppNaming.lookupSubcontext(CronServlet.class.getName());
        final Object o = (context == null) ? null : AppNaming.lookup(context, name);
        if (o instanceof CronService) {
            final CronService cronService = (CronService) o;
            logger.info(cronService.toString());
            final CronConfig config = cronService.getConfig();
            final Collection<CronConfigJob> jobs = config.getJobs();
            for (CronConfigJob job : jobs) {
                logger.info(job.getName());
                cronService.run(job.getName(), null);
            }
        }
        ServletU.write(httpResponse, response);
    }
}
