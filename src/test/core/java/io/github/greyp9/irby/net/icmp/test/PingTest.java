package io.github.greyp9.irby.net.icmp.test;

import io.github.greyp9.arwo.core.time.Stopwatch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.logging.Logger;

public class PingTest {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Test
    @Disabled("not dependable")
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
            Assertions.assertTrue(isReachable);
        }
    }
}
