package liquibase.parser;

import liquibase.configuration.AbstractConfigurationContainer;

/**
 * Configuration container for properties applicable to most {@link liquibase.parser.ChangeLogParser} implementations
 */
public class ChangeLogParserCofiguration extends AbstractConfigurationContainer {

    public static final String SUPPORT_PROPERTY_ESCAPING = "supportPropertyEscaping";

    public ChangeLogParserCofiguration() {
        super("liquibase");

        getContainer().addProperty(SUPPORT_PROPERTY_ESCAPING, Boolean.class)
                .setDescription("Support escaping changelog parameters using a colon. Example: ${:user.name}")
                .setDefaultValue(false)
                .addAlias("enableEscaping");
    }

    public boolean getSupportPropertyEscaping() {
        return getContainer().getValue(SUPPORT_PROPERTY_ESCAPING, Boolean.class);
    }

    public ChangeLogParserCofiguration setSupportPropertyEscaping(boolean support) {
        getContainer().setValue(SUPPORT_PROPERTY_ESCAPING, support);
        return this;
    }
}
