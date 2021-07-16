package liquibase.integration.commandline;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.ConfigurationValueConverter;

import java.io.File;
import java.util.logging.Level;

/**
 * Common configuration settings for integrations
 */
public class LiquibaseCommandLineConfiguration implements AutoloadedConfigurations {

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

        DRIVER = builder.define("driver", Class.class).setDescription("Database driver class").build();
        DATABASE_CLASS = builder.define("databaseClass", Class.class).setDescription("Class to use for Database implementation").build();
        CLASSPATH = builder.define("classpath", String.class).setDescription("Additional classpath entries to use").build();
        DRIVER_PROPERTIES_FILE = builder.define("driverPropertiesFile", String.class)
                                        .setDescription("Driver-specific properties")
                                        .build();
        PROPERTY_PROVIDER_CLASS = builder.define("propertyProviderClass", Class.class)
                                         .setDescription("Implementation of Properties class to provide additional driver properties")
                                         .build();
        PROMPT_FOR_NON_LOCAL_DATABASE = builder.define("promptForNonLocalDatabase", Boolean.class)
                                               .setDescription("Should Liquibase prompt if a non-local database is being accessed")
                                               .build();
        INCLUDE_SYSTEM_CLASSPATH = builder.define("includeSystemClasspath", Boolean.class)
                                          .setDescription("Include the system classpath when resolving classes at runtime")
                                          .setDefaultValue(true).build();
        DEFAULTS_FILE = builder.define("defaultsFile", String.class)
                               .setDescription("File with default Liquibase properties")
                               .setDefaultValue("liquibase.properties")
                               .build();

        SHOULD_RUN = builder.define("shouldRun", Boolean.class)
                .setDescription("Should Liquibase commands execute")
                .setDefaultValue(true)
                .addAliasKey("should.run")
                .build();

        LOG_LEVEL = builder.define("logLevel", Level.class)
                .setDefaultValue(Level.OFF,"Controls which logs get set to stderr AND to any log file. The CLI defaults, if log file set, to SEVERE. Others vary by integration. The official log levels are: OFF, SEVERE, WARNING, INFO, FINE")
                .setValueHandler(ConfigurationValueConverter.LOG_LEVEL)
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
