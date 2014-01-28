package liquibase.context;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionContext {

    private Map<Class, Context> contexts = new HashMap<Class, Context>();

    private ContextValueContainer[] valueContainers;

    public ExecutionContext(ContextValueContainer... valueContainers) {
        if (valueContainers == null) {
            valueContainers = new ContextValueContainer[0];
        }
        this.valueContainers = valueContainers;
    }

    public <T extends Context> T getContext(Class<T> type) {
        if (!contexts.containsKey(type)) {
            contexts.put(type, createContext(type));
        }

        return (T) contexts.get(type);
    }

    protected  <T extends Context> T createContext(Class<T> type) {
        try {
            T context = type.newInstance();
            context.init(new SystemPropertyValueContainer());
            return context;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Cannot create default configuration for context "+type.getName());
        }
    }

    public String describeDefaultLookup(Context.ContextProperty property) {
        List<String> reasons = new ArrayList<String>();
        for (ContextValueContainer container : valueContainers) {
            reasons.add(container.describeDefaultLookup(property));
        }

        return StringUtils.join(reasons, " AND ");
    }
}
