package io.github.greyp9.irby.core.widget.net.dns;

import io.github.greyp9.arwo.core.date.DateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.metric.histogram.core.TimeHistogram;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.time.Stopwatch;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class NameLookupRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Date date;
    private final String host;
    private final int timeout;
    private final String toContext;
    private final String toObject;

    public NameLookupRunnable(final String... params) {
        this(new Date(), params[0], params[1], params[2], params[3]);
    }

    public NameLookupRunnable(final Date date, String host, String timeout, String toContext, String toObject) {
        this.date = DateU.copy(date);
        this.host = host;
        this.timeout = (int) DurationU.toMillisP(timeout);
        this.toContext = toContext;
        this.toObject = toObject;
    }

    @Override
    public void run() {
        final String className = getClass().getName();
        final String methodName = "run()";
        logger.entering(className, methodName);
        try {
            final Stopwatch stopwatch = new Stopwatch(className);
            final InetAddress address = InetAddress.getByName(host);
            final boolean isReachable = address.isReachable(timeout);
            final long elapsed = stopwatch.lap();
            final TimeHistogram histogram = (TimeHistogram) AppNaming.lookup(toContext, toObject);
            histogram.normalize(date);
            histogram.add(date, isReachable ? elapsed : Integer.MAX_VALUE);
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        logger.exiting(className, methodName);
    }
}
