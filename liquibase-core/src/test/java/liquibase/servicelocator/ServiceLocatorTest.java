package liquibase.servicelocator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import liquibase.parser.ChangeLogParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.test.TestContext;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ServiceLocatorTest {
    private ServiceLocator serviceLocator;

    @Before
    public void setup() throws Exception{
        CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(new ClassLoaderResourceAccessor(), TestContext.getInstance().getTestResourceAccessor());

        serviceLocator = ServiceLocator.getInstance();
        serviceLocator.setResourceAccessor(resourceAccessor);
    }

    @After
    public void teardown() {
        ServiceLocator.reset();
    }

    @Test
     public void reset() {
         ServiceLocator instance1 = ServiceLocator.getInstance();
         ServiceLocator.reset();
         assertFalse(instance1 == ServiceLocator.getInstance());
     }


    @Test
    public void getClasses() throws Exception {
        Class[] classes = serviceLocator.findClasses(ChangeLogParser.class);
        assertTrue(classes.length > 0);
    }


    @Test
    @Ignore
    public void getClasses_sampleJar() throws Exception {
        Class[] classes = ServiceLocator.getInstance().findClasses(SqlGenerator.class);
        for (Class clazz : classes) {
            if (clazz.getName().equals("liquibase.sqlgenerator.ext.sample1.Sample1UpdateGenerator")) {
                return;
            }
        }
        fail("Did not find Sample1UpdateGenerator");
    }

    @Test
    public void extractZipFile() throws MalformedURLException {
        File zipFile = ServiceLocator.extractZipFile(new URL("jar:file:/C:/Projects/liquibase2/liquibase-integration-tests/src/test/resources/ext/jars/liquibase-sample1.jar!/liquibase/sqlgenerator"));
        assertEquals("C:/Projects/liquibase2/liquibase-integration-tests/src/test/resources/ext/jars/liquibase-sample1.jar",zipFile.toString().replace('\\','/'));
         zipFile = ServiceLocator.extractZipFile(new URL("jar:file:/home/myuser/liquibase2/liquibase-integration-tests/src/test/resources/ext/jars/liquibase-sample1.jar!/liquibase/sqlgenerator"));
        assertEquals("/home/myuser/liquibase2/liquibase-integration-tests/src/test/resources/ext/jars/liquibase-sample1.jar",zipFile.toString().replace('\\','/'));
    }
}
