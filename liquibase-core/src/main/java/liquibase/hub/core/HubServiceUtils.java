package liquibase.hub.core;

import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.util.StringUtil;

public class HubServiceUtils {
    public static boolean apiKeyExists() {
        HubConfiguration hubConfiguration =
            LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        String apiKey = hubConfiguration.getLiquibaseHubApiKey();
        if (StringUtil.isEmpty(apiKey)) {
            return false;
        }
        return true;
    }
}
