package liquibase.logging.core;

import liquibase.hub.HubConfiguration;
import liquibase.logging.LogMessageFilter;

public class DefaultLogMessageFilter implements LogMessageFilter {

    @Override
    public String filterMessage(String message) {
        String liquibaseHubApiKey = null;
        if (HubConfiguration.LIQUIBASE_HUB_API_KEY != null) {
            liquibaseHubApiKey = HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue();
        }
        if (liquibaseHubApiKey != null) {
            message = message.replace(liquibaseHubApiKey, HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValueObfuscated());
        }

        return message;
    }
}
