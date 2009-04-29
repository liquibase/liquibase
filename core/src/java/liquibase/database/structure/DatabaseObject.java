package liquibase.database.structure;

public interface DatabaseObject {
    DatabaseObject[] getContainingObjects();
}
