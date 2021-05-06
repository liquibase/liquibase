package liquibase.integration;

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
    public static final ConfigurationDefinition<String> DEFAULT_SCHEMA_NAME;
    public static final ConfigurationDefinition<String> DEFAULT_CATALOG_NAME;
    public static final ConfigurationDefinition<String> CLASSPATH;
    public static final ConfigurationDefinition<String> DRIVER_PROPERTIES_FILE;
    public static final ConfigurationDefinition<Class> PROPERTY_PROVIDER_CLASS;
    public static final ConfigurationDefinition<Boolean> PROMPT_FOR_NON_LOCAL_DATABASE;
    public static final ConfigurationDefinition<Boolean> INCLUDE_SYSTEM_CLASSPATH;
    public static final ConfigurationDefinition<String> DEFAULTS_FILE;
    public static final ConfigurationDefinition<Level> LOG_LEVEL;
    public static final ConfigurationDefinition<File> LOG_FILE;
    public static final ConfigurationDefinition<File> OUTPUT_FILE;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase");

        DRIVER = builder.define("driver", Class.class).build();
        DATABASE_CLASS = builder.define("databaseClass", Class.class).build();
        DEFAULT_SCHEMA_NAME = builder.define("defaultSchemaName", String.class).build();
        DEFAULT_CATALOG_NAME = builder.define("defaultCatalogName", String.class).build();
        CLASSPATH = builder.define("classpath", String.class).build();
        DRIVER_PROPERTIES_FILE = builder.define("driverPropertiesFile", String.class).build();
        PROPERTY_PROVIDER_CLASS = builder.define("propertyProviderClass", Class.class).build();
        PROMPT_FOR_NON_LOCAL_DATABASE = builder.define("promptForNonLocalDatabase", Boolean.class).build();
        INCLUDE_SYSTEM_CLASSPATH = builder.define("includeSystemClasspath", Boolean.class).build();
        DEFAULTS_FILE = builder.define("defaultsFile", String.class).build();
        LOG_LEVEL = builder.define("logLevel", Level.class).build();
        LOG_FILE = builder.define("logFile", File.class).build();
        OUTPUT_FILE = builder.define("outputFile", File.class).build();
    }

}
