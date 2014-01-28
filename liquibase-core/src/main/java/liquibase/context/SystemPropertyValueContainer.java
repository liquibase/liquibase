package liquibase.context;

public class SystemPropertyValueContainer implements ContextValueContainer {

    @Override
    public Object getValue(String contextPrefix, String property) {
        return System.getProperty(contextPrefix+"."+property);
    }

    @Override
    public String describeDefaultLookup(Context.ContextProperty property) {
        return "System property '"+property.getContextPrefix()+"."+property.getName()+"'";
    }
}
