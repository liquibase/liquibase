package liquibase.servicelocator;

import liquibase.parser.ChangeLogParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.test.TestContext;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
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
