package io.github.greyp9.irby.core.regex.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class RegexTest {

    @Test
    public void testAnyString() throws Exception {
        Pattern patternAny = Pattern.compile(".*");
        Assert.assertTrue(patternAny.matcher("").matches());
        Assert.assertTrue(patternAny.matcher("ABC").matches());
        Assert.assertTrue(patternAny.matcher("123").matches());
        Assert.assertTrue(patternAny.matcher("!@#").matches());
    }
}
