package liquibase.test;

import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class JUnitResourceAccessor implements ResourceAccessor {
    private URLClassLoader classLoader;

    public JUnitResourceAccessor() throws Exception {
        File srcDir = new File(TestContext.getInstance().findCoreProjectRoot(), "src");
        File integrationClassesDir = new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "target/classes");
        File integrationTestClassesDir = new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "target/test-classes");
         classLoader = new URLClassLoader(new URL[]{
                //integrationClassesDir.toURL(),
                 //integrationTestClassesDir.toURL(),
                //new File(srcDir, "test/java").toURL(),
                 new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "src/test/resources/packaged-changelog.jar").toURL(),
                new File(System.getProperty("java.io.tmpdir")).toURL(),
        });

    }

    public InputStream getResourceAsStream(String file) throws IOException {
        if (file == null) {
            return null;
        }
        return classLoader.getResourceAsStream(file);
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return classLoader.getResources(packageName);
    }

    public ClassLoader toClassLoader() {
        return classLoader;
    }

    @Override
    public String toString() {
        List<String> urls = new ArrayList<String>();
        for (URL url : classLoader.getURLs()) {
            urls.add(url.toExternalForm());
        }
        
        return getClass().getName() + "(" + StringUtils.join(urls, ",") + ")";
    }
}
