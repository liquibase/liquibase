package liquibase.parser;

import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.AutoloadedConfigurations;

/**
 * Configuration container for properties applicable to most {@link liquibase.parser.ChangeLogParser} implementations
 */
public class ChangeLogParserConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<Boolean> SUPPORT_PROPERTY_ESCAPING;
    public static final ConfigurationDefinition<Boolean> USE_PROCEDURE_SCHEMA;
    public static final ConfigurationDefinition<MissingPropertyMode> MISSING_PROPERTY_MODE;

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
                .setDescription("How to handle missing properties. Values can be 'legacy', 'empty' or 'throw'.")
                .setDefaultValue(MissingPropertyMode.LEGACY)
                .build();
    }

    public enum MissingPropertyMode {
        LEGACY,
        EMPTY,
        THROW
    }
}
