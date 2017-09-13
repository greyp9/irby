package io.github.greyp9.irby.core.regex.test;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.regex.Pattern;

public class RegexTest extends TestCase {

    public void testAnyString() throws Exception {
        Pattern patternAny = Pattern.compile(".*");
        Assert.assertTrue(patternAny.matcher("").matches());
        Assert.assertTrue(patternAny.matcher("ABC").matches());
        Assert.assertTrue(patternAny.matcher("123").matches());
        Assert.assertTrue(patternAny.matcher("!@#").matches());
    }
}
