package liquibase.ext;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import org.junit.After;
import org.junit.Before;

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
}
