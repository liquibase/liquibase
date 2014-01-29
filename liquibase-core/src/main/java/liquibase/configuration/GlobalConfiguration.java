package liquibase.configuration;

public class GlobalConfiguration extends AbstractConfiguration {

    public static final String SHOULD_RUN = "shouldRun";

    public GlobalConfiguration() {
        super("liquibase");

        getContainer().addProperty(SHOULD_RUN, Boolean.class)
                .setDescription("Should Liquibase commands execute")
                .setDefaultValue(true)
                .addAlias("liquibase.should.run");
    }

    public boolean getShouldRun() {
        return getContainer().getValue(SHOULD_RUN, Boolean.class);
    }

    public GlobalConfiguration setShouldRun(boolean shouldRun) {
        getContainer().setValue(SHOULD_RUN, shouldRun);
        return this;
    }
}
