package io.github.greyp9.irby.core.regex.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class RegexTest {

    @Test
    public void testAnyString() {
        final Pattern patternAny = Pattern.compile(".*");
        Assert.assertTrue(patternAny.matcher("").matches());
        Assert.assertTrue(patternAny.matcher("ABC").matches());
        Assert.assertTrue(patternAny.matcher("123").matches());
        Assert.assertTrue(patternAny.matcher("!@#").matches());
    }

    @Test
    public void testSplitSpaces() {
        final String[] split = "a b c d e".split("\\s+");
        Assert.assertEquals(5, split.length);
        int token = -1;
        Assert.assertEquals("a", split[++token]);
        Assert.assertEquals("b", split[++token]);
        Assert.assertEquals("c", split[++token]);
        Assert.assertEquals("d", split[++token]);
        Assert.assertEquals("e", split[++token]);
    }
}
