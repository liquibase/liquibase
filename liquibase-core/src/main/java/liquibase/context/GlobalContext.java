package liquibase.context;

public class GlobalContext extends Context {

    public static final String SHOULD_RUN = "shouldRun";

    public GlobalContext() {
        super("liquibase");

        getState().addProperty(SHOULD_RUN, Boolean.class)
                .setDescription("Should Liquibase commands execute")
                .setDefaultValue(true)
                .addAlias("liquibase.should.run");
    }

    public boolean getShouldRun() {
        return getState().getValue(SHOULD_RUN, Boolean.class);
    }

    public GlobalContext setShouldRun(boolean shouldRun) {
        getState().setValue(SHOULD_RUN, shouldRun);
        return this;
    }
}
