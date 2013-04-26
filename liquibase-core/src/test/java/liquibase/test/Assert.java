package liquibase.test;

import java.util.Arrays;
import java.util.Set;

public class Assert
{
    public static void assertSetsEqual(String[] expected, Set<String> set) {
        org.junit.Assert.assertEquals("Set size does not match", expected.length, set.size());
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
}
