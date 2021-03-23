package liquibase.parser;

import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.AutoloadedConfigurations;

/**
 * Configuration container for properties applicable to most {@link liquibase.parser.ChangeLogParser} implementations
 */
public class ChangeLogParserConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<Boolean> SUPPORT_PROPERTY_ESCAPING;
    public static final ConfigurationDefinition<Boolean> USE_PROCEDURE_SCHEMA;

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
    }
}
