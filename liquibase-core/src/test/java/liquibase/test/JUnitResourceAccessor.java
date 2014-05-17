package liquibase.test;

import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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
