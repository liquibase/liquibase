package liquibase.context;

public interface ContextValueContainer {
    public Object getValue(String contextPrefix, String property);

    String describeDefaultLookup(Context.ContextProperty property);
}
