package liquibase.change;

import liquibase.servicelocator.PrioritizedService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Static metadata about a Change
 */
public class ChangeMetaData implements PrioritizedService {
    public static final int PRIORITY_DEFAULT = 1;

    private String name;
    private String description;
    private int priority;

    private Map<String, ChangeParameterMetaData> parameters;
    private String[] appliesTo;

    public ChangeMetaData(String name, String description, int priority, String[] appliesTo, Map<String, ChangeParameterMetaData> parameters) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.parameters = Collections.unmodifiableMap(parameters);
        this.appliesTo = appliesTo;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public Map<String, ChangeParameterMetaData> getParameters() {
        return parameters;
    }

    public String[] getAppliesTo() {
        return appliesTo;
    }
}
