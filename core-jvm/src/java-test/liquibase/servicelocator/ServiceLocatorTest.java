package liquibase.servicelocator;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import liquibase.parser.ChangeLogParser;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.test.TestContext;
import liquibase.servicelocator.ServiceLocator;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;

public class ServiceLocatorTest {
    private ServiceLocator serviceLocator;

    @Before
    public void setup() throws Exception{
        File sample1 = new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/lib-test/liquibase-sample1.jar");
        File sample2 = new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/lib-test/liquibase-sample2.jar");
        CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(new ClassLoaderResourceAccessor(), new ClassLoaderResourceAccessor(new URLClassLoader(new URL[]{
                sample1.toURL(),
                sample2.toURL()
        })));
        
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
    public void getClasses_sampleJar() throws Exception {
        Class[] classes = ServiceLocator.getInstance().findClasses(SqlGenerator.class);
        for (Class clazz : classes) {
            if (clazz.getName().equals("liquibase.sqlgenerator.ext.sample1.Sample1UpdateGenerator")) {
                return;
            }
        }
        fail("Did not find Sample1UpdateGenerator");
    }
}
