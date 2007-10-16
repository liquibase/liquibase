package liquibase.database.structure;

public class Schema implements DatabaseObject {
    private String name;


    public Schema(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
