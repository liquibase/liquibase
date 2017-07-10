package liquibase.resource;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class AbstractResourceAccessorJavaTest {
    @Test
    public void testConvertToPathRelativeDoesntGenerateDoubleSlahes() {
        AbstractResourceAccessor ara = new MyARA();
        URL rootPathURL = ara.toClassLoader().getResource("liquibase/resource/");
        ara.addRootPath(rootPathURL);
        String path = ara.convertToPath("liquibase/resource/empty.txt", "changelogs/");
        //liquibase.resource.AbstractResourceAccessor.convertToPath(String, String) introduces a double slash
        //then in liquibase.resource.AbstractResourceAccessor.convertToPath(String), if it matches the part
        //before the double slash, then an absolute path is generated instead of a relative one (E.g. '/changelogs/'
        //instead of 'changelogs/').
        assertEquals("changelogs/", path);
    }

    private void checkConvertToPathIsConsistentGivenTheRootPathInsertionOrder(
            String prefix) throws MalformedURLException {
        AbstractResourceAccessor ara = new MyARA();
        ara.addRootPath(new URL(prefix + "logs/"));
        ara.addRootPath(new URL(prefix));
        //System.out.println(ara.getRootPaths());
        String path = ara.convertToPath(prefix + "logs/cs-1.0.xml");
        assertEquals("cs-1.0.xml", path);
    }

    private static final class MyARA extends AbstractResourceAccessor {

        @Override
        protected void init() {
            //We don't pollute the tests with external rootPaths
        }

        @Override
        public Set<InputStream> getResourcesAsStream(String path)
                throws IOException {
            return null;
        }

        @Override
        public Set<String> list(String relativeTo, String path,
                boolean includeFiles, boolean includeDirectories, boolean recursive)
                throws IOException {
            return null;
        }

        @Override
        public ClassLoader toClassLoader() {
            return Thread.currentThread().getContextClassLoader();
        }

    }

}
