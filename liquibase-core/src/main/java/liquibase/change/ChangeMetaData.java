package liquibase.change;

import java.util.Collections;
import java.util.Set;

import liquibase.servicelocator.PrioritizedService;

public class ChangeMetaData implements PrioritizedService {
    public static final int PRIORITY_DEFAULT = 1;

    private String name;
    private String description;
    private int priority;

    private Set<ChangeParameterMetaData> parameters;
    private String[] appliesTo;

    public ChangeMetaData(String name, String description, int priority, String[] appliesTo, Set<ChangeParameterMetaData> parameters) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.parameters = Collections.unmodifiableSet(parameters);
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

    public Set<ChangeParameterMetaData> getParameters() {
        return parameters;
    }

    public String[] getAppliesTo() {
        return appliesTo;
    }
}
