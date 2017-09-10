package io.github.greyp9.irby.net.icmp.test;

import io.github.greyp9.arwo.core.time.Stopwatch;
import junit.framework.TestCase;
import org.junit.Assert;

import java.net.InetAddress;
import java.util.logging.Logger;

public class PingTest extends TestCase {
    Logger logger = Logger.getLogger(getClass().getName());

    public void testPing() throws Exception {
        String[] hostnames = {
                "amazon.com",
        };
        for (String hostname : hostnames) {
            Stopwatch stopwatch = new Stopwatch("name lookup");
            InetAddress address = InetAddress.getByName(hostname);
            final boolean isReachable = address.isReachable(3000);
            final long elapsed = stopwatch.lap();
            logger.info(String.format("[address=%s, isReachable=%s, elapsed=%d]",
                    address.getHostAddress(), Boolean.toString(isReachable), elapsed));
            Assert.assertTrue(isReachable);
        }
    }
}
