package liquibase.integration;

import liquibase.Scope;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

import java.io.File;
import java.util.logging.Level;

/**
 * Common configuration settings for integrations
 */
public class IntegrationConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<Class> DRIVER;
    public static final ConfigurationDefinition<Class> DATABASE_CLASS;
    public static final ConfigurationDefinition<String> CLASSPATH;
    public static final ConfigurationDefinition<String> DRIVER_PROPERTIES_FILE;
    public static final ConfigurationDefinition<Class> PROPERTY_PROVIDER_CLASS;
    public static final ConfigurationDefinition<Boolean> PROMPT_FOR_NON_LOCAL_DATABASE;
    public static final ConfigurationDefinition<Boolean> INCLUDE_SYSTEM_CLASSPATH;
    public static final ConfigurationDefinition<String> DEFAULTS_FILE;
    public static final ConfigurationDefinition<Level> LOG_LEVEL;
    public static final ConfigurationDefinition<File> LOG_FILE;
    public static final ConfigurationDefinition<File> OUTPUT_FILE;
    public static final ConfigurationDefinition<Boolean> SHOULD_RUN;
    public static final ConfigurationDefinition<ArgumentConverter> ARGUMENT_CONVERTER;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase");

        DRIVER = builder.define("driver", Class.class).build();
        DATABASE_CLASS = builder.define("databaseClass", Class.class).build();
        CLASSPATH = builder.define("classpath", String.class).build();
        DRIVER_PROPERTIES_FILE = builder.define("driverPropertiesFile", String.class).build();
        PROPERTY_PROVIDER_CLASS = builder.define("propertyProviderClass", Class.class).build();
        PROMPT_FOR_NON_LOCAL_DATABASE = builder.define("promptForNonLocalDatabase", Boolean.class).build();
        INCLUDE_SYSTEM_CLASSPATH = builder.define("includeSystemClasspath", Boolean.class).setDefaultValue(true).build();
        DEFAULTS_FILE = builder.define("defaultsFile", String.class).setDefaultValue("liquibase.properties").build();

        SHOULD_RUN = builder.define("shouldRun", Boolean.class)
                .setDescription("Should Liquibase commands execute")
                .setDefaultValue(true)
                .addAliasKey("should.run")
                .build();

        LOG_LEVEL = builder.define("logLevel", Level.class)
                .setDefaultValue(Level.INFO)
                .setValueHandler(value -> {
                    if (value == null) {
                        return null;
                    }
                    if (value instanceof Level) {
                        return (Level) value;
                    }
                    String stringLevel = String.valueOf(value).toUpperCase();
                    if (stringLevel.equals("DEBUG")) {
                        return Level.FINE;
                    } else if (stringLevel.equals("WARN")) {
                        return Level.WARNING;
                    } else if (stringLevel.equals("ERROR")) {
                        return Level.SEVERE;
                    }

                    try {
                        return Level.parse(stringLevel);
                    } catch (IllegalArgumentException e) {
                        Scope.getCurrentScope().getUI().sendErrorMessage("Unknown log level " + stringLevel);
                        return Level.INFO;
                    }
                })
                .build();

        LOG_FILE = builder.define("logFile", File.class).build();
        OUTPUT_FILE = builder.define("outputFile", File.class).build();

        ARGUMENT_CONVERTER = builder.define("argumentConverter", ArgumentConverter.class)
                .setInternal(true)
                .setDescription("Configured by the integration to convert arguments in user messages to something that matches the formats they expect")
                .setDefaultValue(argument -> argument)
                .build();
    }

    public interface ArgumentConverter {
        String convert(String argument);
    }

}
