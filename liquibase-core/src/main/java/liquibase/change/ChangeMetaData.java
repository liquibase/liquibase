package liquibase.change;

import liquibase.servicelocator.PrioritizedService;

public class ChangeMetaData implements PrioritizedService {
    public static final int PRIORITY_DEFAULT = 1;

    private String name;
    private String description;
    private int priority;

    public ChangeMetaData(String name, String description, int priority) {
        this.name = name;
        this.description = description;
        this.priority = priority;

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
        this.priority = priority;
    }
}
