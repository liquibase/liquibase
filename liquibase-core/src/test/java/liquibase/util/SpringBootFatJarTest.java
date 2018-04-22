package liquibase.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringBootFatJarTest {

    @Test
    public void testGetPathForResourceWithFatJarPath() {
        String result = SpringBootFatJar.getPathForResource("some/path!/that/has!/two/bangs");
        assertEquals("that/has/two/bangs", result);
    }

    @Test
    public void testGetPathForNestedJarResourceWithFatJarPath() {
        String result = SpringBootFatJar.getPathForResource("some/path!/that/has/jar.jar!/one/bangs");
        assertEquals("that/has/jar.jar", result);
    }

    @Test
    public void testGetPathForResourceWithSimplePath() {
        String result = SpringBootFatJar.getPathForResource("some/path!/that/has/one/bang");
        assertEquals("some/path!/that/has/one/bang", result);
    }

    @Test
    public void testGetSimplePathForResourceWithFatJarPath() {
        String result = SpringBootFatJar.getSimplePathForResources("/that/has/two/bangs/entryname","some/path!/that/has!/two/bangs");
        assertEquals("two/bangs/entryname", result);
    }

    @Test
    public void testGetSimplePathForResourceWithSimplePath() {
        String result = SpringBootFatJar.getSimplePathForResources("/that/has/one/bang","some/path!/that/has/one/bang");
        assertEquals("/that/has/one/bang", result);
    }
}
