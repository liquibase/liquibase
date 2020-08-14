package liquibase.logging.core;

import liquibase.Scope;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.logging.LogMessageFilter;
import liquibase.util.StringUtil;

public class DefaultLogMessageFilter implements LogMessageFilter {

    @Override
    public String filterMessage(String message) {
        final HubConfiguration configuration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);

        final String liquibaseHubApiKey = StringUtil.trimToNull(configuration.getLiquibaseHubApiKey());
        if (liquibaseHubApiKey != null) {
            message = message.replace(liquibaseHubApiKey, configuration.getLiquibaseHubApiKeySecureDescription());
        }

        return message;
    }
}
