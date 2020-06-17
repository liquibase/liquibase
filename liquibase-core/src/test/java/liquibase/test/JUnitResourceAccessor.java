package liquibase.test;

import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class JUnitResourceAccessor extends ClassLoaderResourceAccessor {

    public JUnitResourceAccessor() throws Exception {
        super(new URLClassLoader(new URL[]{
                new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "src/test/resources/packaged-changelog.jar").toURI().toURL(),
                new File(System.getProperty("java.io.tmpdir")).toURI().toURL(),
        }));

    }
}
