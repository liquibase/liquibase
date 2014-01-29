package liquibase.configuration;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;

public class LiquibaseConfigurationTest {

    @Test
    public void getContext_defaultSetup() {
        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();
        GlobalConfiguration globalConfiguration = liquibaseConfiguration.getConfiguration(GlobalConfiguration.class);

        assertNotNull(globalConfiguration);

        assertSame("Multiple calls to getConfiguration should return the same instance", globalConfiguration, liquibaseConfiguration.getConfiguration(GlobalConfiguration.class));
    }
}
