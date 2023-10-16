package io.github.greyp9.irby.core.regex.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
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
        final int lengthExpected = 5;
        Assert.assertEquals(lengthExpected, split.length);
        int token = -1;
        Assert.assertEquals("a", split[++token]);
        Assert.assertEquals("b", split[++token]);
        Assert.assertEquals("c", split[++token]);
        Assert.assertEquals("d", split[++token]);
        Assert.assertEquals("e", split[++token]);
    }

    @Test
    public void testMultiline() {
        final Pattern pattern = Pattern.compile("(?s).+blob(.*)stock.+");
        final String text = "foo\nblob\nsome text\nstock\nword";
        final Matcher matcher = pattern.matcher(text);
        final boolean matches = matcher.matches();
        Assert.assertTrue(matches);
        final String group1 = matcher.group(1);
        Assert.assertEquals("\nsome text\n", group1);
    }
}
