package liquibase.parser;

import liquibase.configuration.AbstractConfigurationContainer;

/**
 * Configuration container for properties applicable to most {@link liquibase.parser.ChangeLogParser} implementations
 */
public class ChangeLogParserCofiguration extends AbstractConfigurationContainer {

    public static final String SUPPORT_PROPERTY_ESCAPING = "supportPropertyEscaping";
    public static final String USE_PROCEDURE_SCHEMA = "useProcedureSchema";

    public ChangeLogParserCofiguration() {
        super("liquibase");

        getContainer().addProperty(SUPPORT_PROPERTY_ESCAPING, Boolean.class)
                .setDescription("Support escaping changelog parameters using a colon. Example: ${:user.name}")
                .setDefaultValue(false)
                .addAlias("enableEscaping");

        getContainer().addProperty(USE_PROCEDURE_SCHEMA, Boolean.class)
                .setDescription("If set to true (default value), createProcedure tags with a set schemaName will modify the procedure body with the given schema name.")
                .setDefaultValue(true);
    }

    public boolean getSupportPropertyEscaping() {
        return getContainer().getValue(SUPPORT_PROPERTY_ESCAPING, Boolean.class);
    }

    public ChangeLogParserCofiguration setSupportPropertyEscaping(boolean support) {
        getContainer().setValue(SUPPORT_PROPERTY_ESCAPING, support);
        return this;
    }

    public boolean getUseProcedureSchema() {
        return getContainer().getValue(USE_PROCEDURE_SCHEMA, Boolean.class);
    }

    public ChangeLogParserCofiguration setUseProcedureSchema(boolean useSchema) {
        getContainer().setValue(USE_PROCEDURE_SCHEMA, useSchema);
        return this;
    }


}
