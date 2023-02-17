package liquibase.integration.commandline;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.logging.LogFormat;
import liquibase.logging.LogOutputStream;
import liquibase.util.StringUtil;

import java.util.Arrays;
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
    public static final ConfigurationDefinition<String> LOG_CHANNELS;
    public static final ConfigurationDefinition<String> LOG_FILE;
    public static final ConfigurationDefinition<LogFormat> LOG_FORMAT;
    public static final ConfigurationDefinition<LogOutputStream> LOG_STREAM;
    public static final ConfigurationDefinition<String> OUTPUT_FILE;
    public static final ConfigurationDefinition<Boolean> SHOULD_RUN;
    public static final ConfigurationDefinition<ArgumentConverter> ARGUMENT_CONVERTER;
    public static final ConfigurationDefinition<String> MONITOR_PERFORMANCE;

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

        LOG_CHANNELS = builder.define("logChannels", String.class)
                .setDefaultValue("liquibase", "Controls which log channels have their level set by the liquibase.logLevel setting. Comma separate multiple values. To set the level of all channels, use 'all'. Example: liquibase,org.mariadb.jdbc")
                .build();

        LOG_FILE = builder.define("logFile", String.class).build();
        OUTPUT_FILE = builder.define("outputFile", String.class).build();

        MONITOR_PERFORMANCE = builder.define("monitorPerformance", String.class)
                .setDescription("Enable performance tracking. Set to 'false' to disable. If set to 'true', data is stored to a `liquibase-TIMESTAMP.jfr` file in your working directory. Any other value will enable tracking and be used as the name of the file to write the data to.")
                .setDefaultValue("false")
                .build();

        ARGUMENT_CONVERTER = builder.define("argumentConverter", ArgumentConverter.class)
                .setInternal(true)
                .setDescription("Configured by the integration to convert arguments in user messages to something that matches the formats they expect")
                .setDefaultValue(argument -> argument)
                .build();

        LOG_FORMAT = builder.define("logFormat", LogFormat.class)
                .setDescription("Sets the format of log output to console or log files. " +
                        "Open Source users default to unstructured \"" + LogFormat.TEXT + "\" logs to the console or output log files. " +
                        "Pro users have the option to set value as \"" + LogFormat.JSON + "\" or \"" + LogFormat.JSON_PRETTY + "\" to enable json-structured log files to the console or output log files.")
                .setDefaultValue(LogFormat.TEXT)
                .setHidden(true)
                .setValueHandler((logFormat) -> {
                    if (logFormat == null) {
                        return null;
                    }

                    if (logFormat instanceof String) {
                        String logFormatString = (String) logFormat;

                        if (Arrays.stream(LogFormat.values()).noneMatch(lf -> lf.toString().equalsIgnoreCase(logFormatString))) {
                            throw new IllegalArgumentException("WARNING: The log format value '"+logFormatString+"' is not valid. Valid values include: '" + StringUtil.join(LogFormat.values(), "', '", Object::toString) + "'");
                        }

                        return Enum.valueOf(LogFormat.class, logFormatString.toUpperCase());
                    } else if (logFormat instanceof LogFormat) {
                        return (LogFormat) logFormat;
                    } else {
                        return null;
                    }
                })
                .build();

        LOG_STREAM = builder.define("logStream", LogOutputStream.class)
                .setDescription("Redirect all log output to " + LogOutputStream.STDOUT + " or " + LogOutputStream.STDERR + ".")
                .setDefaultValue(null)
                .setHidden(true)
                .setValueHandler((stream) -> {
                    if (stream == null) {
                        return null;
                    } else if (stream instanceof String) {
                        String streamString = (String) stream;
                        return LogOutputStream.getStream(streamString);
                    } else if (stream instanceof LogOutputStream) {
                        return (LogOutputStream) stream;
                    }
                    return null;
                })
                .build();
   }

    public interface ArgumentConverter {
        String convert(String argument);
    }

}
