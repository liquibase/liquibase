package liquibase.change;

public class ChangeMetaData {
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

}
