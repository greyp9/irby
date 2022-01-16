package io.github.greyp9.irby.net.dns.test;

import io.github.greyp9.arwo.core.time.Stopwatch;
import org.junit.Test;

import java.net.InetAddress;
import java.util.logging.Logger;

public class NameLookupTest {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Test
    public void testNameLookup() throws Exception {
        String[] hostnames = {
                "google.com",
        };
        for (String hostname : hostnames) {
            Stopwatch stopwatch = new Stopwatch("name lookup");
            InetAddress address = InetAddress.getByName(hostname);
            final long elapsed = stopwatch.lap();
            logger.info(String.format("[address=%s, elapsed=%d]",
                    address.getHostAddress(), elapsed));
        }
    }
}
