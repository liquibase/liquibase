package liquibase.ext;

import static org.junit.Assert.fail;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.test.TestContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExtServiceLocatorTest {
    private ServiceLocator serviceLocator;

    @Before
    public void setup() throws Exception{
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

        serviceLocator = ServiceLocator.getInstance();
        serviceLocator.setResourceAccessor(resourceAccessor);
    }

    @After
    public void teardown() {
        ServiceLocator.reset();
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
