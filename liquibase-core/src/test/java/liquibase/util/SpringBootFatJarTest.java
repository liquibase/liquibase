package liquibase.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringBootFatJarTest {

//    @Test
//    public void testGetPathForResourceWithTwoBangs() {
//        String result = SpringBootFatJar.getPathForResource("some/path!/that/has!/two/bangs");
//        assertEquals(result, "that/has/two/bangs");
//    }

//    @Test
//    public void testGetPathForResourceWithOneBang() {
//        String result = SpringBootFatJar.getPathForResource("some/path!/that/has/one/bang");
//        assertEquals("that/has/one/bang", result);
//    }

    @Test
    public void testGetPathForResourceWithSimplePath() {
        String result = SpringBootFatJar.getPathForResource("some/simple/path");
        assertEquals("some/simple/path", result);
    }

    @Test
    public void testGetPathForResourceWithSpringBootFatJar() {
        String path = "jar:file:/some/fat.jar!/BOOT-INF/lib/some.jar!/db/changelogs";
        String result = SpringBootFatJar.getPathForResource(path);
        assertEquals("BOOT-INF/lib/some.jar", result);

        path = "jar:file:/some/fat.jar!/BOOT-INF/classes!/db/changelogs";
        result = SpringBootFatJar.getPathForResource(path);
        assertEquals("BOOT-INF/classes/db/changelogs", result);
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

    @Test
    public void testGetSimplePathForResourceWithNestedJarPath() {
        String entryName = "BOOT-INF/lib/some.jar";
        String path = "jar:file:/some/fat.jar!/BOOT-INF/lib/some.jar!/db/changelogs";
        String result = SpringBootFatJar.getSimplePathForResources(entryName, path);
        assertEquals("db/changelogs", result);
    }

}
