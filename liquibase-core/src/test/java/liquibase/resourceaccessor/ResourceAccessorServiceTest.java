package liquibase.resourceaccessor;

import liquibase.resource.ResourceAccessor;
import liquibase.resource.ResourceAccessorService;
import liquibase.resource.ResourceAccessorServiceFactory;
import liquibase.resource.StandardResourceAccessorService;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceAccessorServiceTest {

    private ResourceAccessorService resourceAccessorService;

    @Before
    public void setUp() {
        resourceAccessorService = ResourceAccessorServiceFactory.getInstance().getResourceAccessorService();
    }

    @Test
    public void getResourceAccessorService() {
        assertThat(resourceAccessorService).isInstanceOf(StandardResourceAccessorService.class);
    }

    @Test
    public void getTestResourceAccessorService() {
        ResourceAccessorServiceFactory.getInstance().register(new TestResourceAccessorService());
        resourceAccessorService = ResourceAccessorServiceFactory.getInstance().getResourceAccessorService();
        assertThat(resourceAccessorService).isInstanceOf(TestResourceAccessorService.class);
    }

    private static class TestResourceAccessorService extends StandardResourceAccessorService {
        @Override
        public ResourceAccessor getResourceAccessor(ClassLoader classLoader) {
            return null;
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }
}