package liquibase.configuration;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class LiquibaseConfigurationTest {

    @Test
    public void getContext_defaultSetup() {
        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();
        GlobalConfiguration globalConfiguration = liquibaseConfiguration.getConfiguration(GlobalConfiguration.class);

        assertNotNull(globalConfiguration);

        assertSame("Multiple calls to getConfiguration should return the same instance", globalConfiguration, liquibaseConfiguration.getConfiguration(GlobalConfiguration.class));
    }

    @Test
    public void setGetHubApiKey() {
        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();
        HubConfiguration hubConfiguration = liquibaseConfiguration.getConfiguration(HubConfiguration.class);
        hubConfiguration.setLiquibaseHubApiKey("this_is_a_hub_key");
        String hubApiKey = hubConfiguration.getLiquibaseHubApiKey();
        Assert.assertEquals(hubApiKey,"this_is_a_hub_key");
    }

    @Test
    public void setGetHubUrl() {
        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();
        HubConfiguration hubConfiguration = liquibaseConfiguration.getConfiguration(HubConfiguration.class);
        hubConfiguration.setLiquibaseHubUrl("https://myhub.liquibase.com/api/v1/");
        String hubUrl = hubConfiguration.getLiquibaseHubUrl();
        Assert.assertEquals(hubUrl,"https://myhub.liquibase.com/api/v1/");
    }

    @Test
    public void setGetHubUrlDefault() {
        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();
        HubConfiguration hubConfiguration = liquibaseConfiguration.getConfiguration(HubConfiguration.class);
        String hubUrl = hubConfiguration.getLiquibaseHubUrl();
        Assert.assertEquals(hubUrl,"https://hub.liquibase.com/api/v1/");
    }
}
