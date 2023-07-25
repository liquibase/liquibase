package liquibase.parser;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

/**
 * Configuration container for properties applicable to most {@link liquibase.parser.ChangeLogParser} implementations
 */
public class ChangeLogParserConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<Boolean> SUPPORT_PROPERTY_ESCAPING;
    public static final ConfigurationDefinition<Boolean> USE_PROCEDURE_SCHEMA;
    public static final ConfigurationDefinition<MissingPropertyMode> MISSING_PROPERTY_MODE;
    public static final ConfigurationDefinition<ChangelogParseMode> CHANGELOG_PARSE_MODE;
    public static final ConfigurationDefinition<MissingIncludeConfiguration> ON_MISSING_INCLUDE_CHANGELOG;
    public static final ConfigurationDefinition<Boolean> ERROR_ON_CIRCULAR_INCLUDE_ALL;
    public static final ConfigurationDefinition<MissingIncludeConfiguration> ON_MISSING_SQL_FILE;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase");

        SUPPORT_PROPERTY_ESCAPING = builder.define("supportPropertyEscaping", Boolean.class)
                .setDescription("Support escaping changelog parameters using a colon. Example: ${:user.name}")
                .setDefaultValue(false)
                .addAliasKey("enableEscaping")
                .build();

        USE_PROCEDURE_SCHEMA = builder.define("useProcedureSchema", Boolean.class)
                .setDescription("If set to true (default value), createProcedure tags with a set schemaName will modify the procedure body with the given schema name.")
                .setDefaultValue(true)
                .build();

        MISSING_PROPERTY_MODE = builder.define("missingPropertyMode", MissingPropertyMode.class)
                .setDescription("How to handle changelog property expressions where a value is not set. For example, a string '${address}' when no 'address' property was defined. Values can be: 'preserve' which leaves the string as-is, 'empty' which replaces it with an empty string, or 'error' which stops processing with an error.")
                .setDefaultValue(MissingPropertyMode.PRESERVE)
                .build();


        CHANGELOG_PARSE_MODE = builder.define("changelogParseMode", ChangelogParseMode.class)
                .setDescription("Configures how to handle unknown fields in changelog files. Possible values: STRICT which causes parsing to fail, and LAX which continues with the parsing.")
                .setDefaultValue(ChangelogParseMode.STRICT)
                .build();

        ERROR_ON_CIRCULAR_INCLUDE_ALL = builder.define("errorOnCircularIncludeAll", Boolean.class)
                .setDescription("Throw an error if Liquibase detects that an includeAll will cause a circular reference (and thus a changelog parse error).")
                .setDefaultValue(true)
                .build();

        ON_MISSING_INCLUDE_CHANGELOG = builder.define("onMissingIncludeChangelog", MissingIncludeConfiguration.class)
                .setDescription("If set to WARN, then liquibase will not throw exception on missing changelog file, instead will show a warning message.")
                .setDefaultValue(MissingIncludeConfiguration.FAIL)
                .build();

        ON_MISSING_SQL_FILE = builder.define("onMissingSqlFile", MissingIncludeConfiguration.class)
                .setDescription("If set to WARN, then Liquibase will not throw exception on missing sqlFile inside a sqlFile change type, instead will show a warning message")
                .setDefaultValue(MissingIncludeConfiguration.FAIL)
                .setHidden(true)
                .build();
    }

    public enum MissingPropertyMode {
        PRESERVE,
        EMPTY,
        ERROR,
    }

    public enum ChangelogParseMode {
        STRICT,
        LAX,
    }

    public enum MissingIncludeConfiguration {
        WARN,
        FAIL
    }
}
