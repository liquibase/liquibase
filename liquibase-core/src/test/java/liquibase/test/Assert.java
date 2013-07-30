package liquibase.test;

import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Assert
{
    public static void assertSetsEqual(String[] expected, Set<String> set) {
        org.junit.Assert.assertEquals("Set size does not match: "+ StringUtils.join(expected, ",")+" vs "+StringUtils.join(set, ","), expected.length, set.size());
        for (String string : expected) {
            org.junit.Assert.assertTrue("Missing expected element " + string, set.contains(string));
        }
        for (String found : set) {
            org.junit.Assert.assertTrue("Unexpected element in set: " + found, Arrays.asList(expected).contains(found));
        }
    }

    public static void assertArraysEqual(String[] expected, String[] array) {
        org.junit.Assert.assertEquals("Set size does not match", expected.length, array.length);

        for (int i=0; i<expected.length; i++) {
            org.junit.Assert.assertEquals("Difference in element "+i, expected[i], array[i]);
        }
    }

    public static void assertListsEqual(Object[] expected, List list, AssertFunction assertFunction) {
        org.junit.Assert.assertEquals("List size does not match", expected.length, list.size());

        for (int i=0; i<expected.length; i++) {
            assertFunction.check("Difference in element "+i, expected[i], list.get(i));
        }
    }

    public abstract static class AssertFunction {
        public abstract void check(String message, Object expected, Object actual);
    }
}
