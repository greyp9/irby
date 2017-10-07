package io.github.greyp9.irby.core.widget.net.icmp;

import io.github.greyp9.arwo.core.date.DateU;
import io.github.greyp9.arwo.core.date.DurationU;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.metric.histogram.core.TimeHistogram;
import io.github.greyp9.arwo.core.naming.AppNaming;
import io.github.greyp9.arwo.core.time.Stopwatch;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class PingRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final String name;
    private final Date date;
    private final String host;
    private final int timeout;
    private final String toContext;
    private final String toObject;

    public PingRunnable(final String name, final Date date, final String... params) {
        this(name, date, params[0], params[1], params[2], params[3]);
    }

    public PingRunnable(final String name, final Date date,
                        String host, String timeout, String toContext, String toObject) {
        this.name = name;
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
            logger.fine(String.format("%s:[%s]:%d:%s:%d", name, XsdDateU.toXSDZMillis(date),
                    timeout, toObject, elapsed));
            recordMetric(isReachable, elapsed);
        } catch (IOException e) {
            logger.severe(e.getMessage());
            recordMetric(false, Integer.MAX_VALUE);
        }
        logger.exiting(className, methodName);
    }

    private void recordMetric(boolean isReachable, long elapsed) {
        final TimeHistogram histogram = (TimeHistogram) AppNaming.lookup(toContext, toObject);
        if (histogram != null) {
            histogram.normalize(date);
            histogram.add(date, isReachable ? elapsed : Integer.MAX_VALUE);
        }
    }
}
