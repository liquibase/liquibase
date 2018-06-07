package liquibase.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringBootFatJarTest {

    @Test
    public void testGetPathForResourceWithTwoBangs() {
        String result = SpringBootFatJar.getPathForResource("some/path!/that/has!/two/bangs");
        assertEquals(result, "that/has/two/bangs");
    }

    @Test
    public void testGetPathForResourceWithOneBang() {
        String result = SpringBootFatJar.getPathForResource("some/path!/that/has/one/bang");
        assertEquals(result, "that/has/one/bang");
    }

    @Test
    public void testGetPathForResourceWithSimplePath() {
        String result = SpringBootFatJar.getPathForResource("some/path/that/has/no/bangs");
        assertEquals(result, "some/path/that/has/no/bangs");
    }
}