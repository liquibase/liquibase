package liquibase.change;

public class ChangeMetaData {
    private String name;
    private String description;

    public ChangeMetaData(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
