package liquibase.logging.core;

import liquibase.configuration.AbstractConfiguration;

public class DefaultLoggerConfiguration extends AbstractConfiguration {

    public static final String LOG_LEVEL = "level";

    public DefaultLoggerConfiguration() {
        super("liquibase.defaultlogger");

        getContainer().addProperty(LOG_LEVEL, String.class)
                .setDescription("Logging level")
                .setDefaultValue("INFO");
    }

    public String getLogLevel() {
        return getContainer().getValue(LOG_LEVEL, String.class);
    }

    public DefaultLoggerConfiguration setLogLevel(String name) {
        getContainer().setValue(LOG_LEVEL, name);
        return this;
    }
}
