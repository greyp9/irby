package io.github.greyp9.irby.core.regex.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

    @Test
    public void testAnyString() {
        final Pattern patternAny = Pattern.compile(".*");
        Assertions.assertTrue(patternAny.matcher("").matches());
        Assertions.assertTrue(patternAny.matcher("ABC").matches());
        Assertions.assertTrue(patternAny.matcher("123").matches());
        Assertions.assertTrue(patternAny.matcher("!@#").matches());
    }

    @Test
    public void testSplitSpaces() {
        final String[] split = "a b c d e".split("\\s+");
        final int lengthExpected = 5;
        Assertions.assertEquals(lengthExpected, split.length);
        int token = -1;
        Assertions.assertEquals("a", split[++token]);
        Assertions.assertEquals("b", split[++token]);
        Assertions.assertEquals("c", split[++token]);
        Assertions.assertEquals("d", split[++token]);
        Assertions.assertEquals("e", split[++token]);
    }

    @Test
    public void testMultiline() {
        final Pattern pattern = Pattern.compile("(?s).+blob(.*)stock.+");
        final String text = "foo\nblob\nsome text\nstock\nword";
        final Matcher matcher = pattern.matcher(text);
        final boolean matches = matcher.matches();
        Assertions.assertTrue(matches);
        final String group1 = matcher.group(1);
        Assertions.assertEquals("\nsome text\n", group1);
    }
}
