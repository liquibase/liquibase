package liquibase.database.structure;

public interface DatabaseObject {
    DatabaseObject[] getContainingObjects();
    
    public String getName();

    Schema getSchema();
}
