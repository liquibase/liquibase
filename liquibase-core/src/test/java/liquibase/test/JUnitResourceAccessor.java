package liquibase.test;

import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class JUnitResourceAccessor extends ClassLoaderResourceAccessor {

    public JUnitResourceAccessor() throws Exception {
//        File srcDir = new File(TestContext.getInstance().findCoreProjectRoot(), "src");
//        File integrationClassesDir = new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "target/classes");
//        File integrationTestClassesDir = new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "target/test-classes");
//         classLoader = new URLClassLoader(new URL[]{
//                //integrationClassesDir.toURL(),
//                 //integrationTestClassesDir.toURL(),
//                //new File(srcDir, "test/java").toURL(),
//                 new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "src/test/resources/packaged-changelog.jar").toURL(),
//                new File(System.getProperty("java.io.tmpdir")).toURL(),
//        });

        super(new URLClassLoader(new URL[]{
                //integrationClassesDir.toURL(),
                //integrationTestClassesDir.toURL(),
                //new File(srcDir, "test/java").toURL(),
                new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "src/test/resources/packaged-changelog.jar").toURL(),
                new File(System.getProperty("java.io.tmpdir")).toURL(),
        }));

    }
}
