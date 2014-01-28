package liquibase.context;

public class ChangeLogParserContext extends Context {

    public static final String SUPPORT_PROPERTY_ESCAPING = "supportPropertyEscaping";

    public ChangeLogParserContext() {
        super("liquibase");

        getState().addProperty(SUPPORT_PROPERTY_ESCAPING, Boolean.class)
                .setDescription("Support escaping changelog parameters using a colon. Example: ${:user.name}")
                .setDefaultValue(false)
                .addAlias("enableEscaping");
    }

    public boolean getSupportPropertyEscaping() {
        return getState().getValue(SUPPORT_PROPERTY_ESCAPING, Boolean.class);
    }

    public ChangeLogParserContext setSupportPropertyEscaping(boolean support) {
        getState().setValue(SUPPORT_PROPERTY_ESCAPING, support);
        return this;
    }
}
