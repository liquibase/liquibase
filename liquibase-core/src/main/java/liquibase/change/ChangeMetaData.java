package liquibase.change;

import liquibase.servicelocator.PrioritizedService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChangeMetaData implements PrioritizedService {
    public static final int PRIORITY_DEFAULT = 1;

    private String name;
    private String description;
    private int priority;

    private Set<ChangeParameterMetaData> parameters;

    public ChangeMetaData(String name, String description, int priority, Set<ChangeParameterMetaData> parameters) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.parameters = Collections.unmodifiableSet(parameters);
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

    public void setPriority(int newPriority) {
        this.priority = newPriority;
    }

    public Set<ChangeParameterMetaData> getParameters() {
        return parameters;
    }
}
