package liquibase.test;

import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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

    @Override
    public InputStream getResourceAsStream(String file) throws IOException {
        if (file == null) {
            return null;
        }
        return classLoader.getResourceAsStream(file);
    }

    @Override
    public Enumeration<URL> getResources(String packageName) throws IOException {
        try {
            URL fileUrl = classLoader.getResource(packageName);
            if (fileUrl != null) {
                File file = new File(fileUrl.toURI());
                if (file.exists() && ! file.isDirectory()) {
                    return new Vector<URL>(Arrays.asList(fileUrl)).elements();
                }
            }
        } catch (Throwable e) {
            //not local file, continue on
        }

        return classLoader.getResources(packageName);
    }

    @Override
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
