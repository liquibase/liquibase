package liquibase.database.structure;

import liquibase.database.Database;

public interface DatabaseObject {
    DatabaseObject[] getContainingObjects();
    
    String getName();

    Schema getSchema();

    boolean equals(DatabaseObject otherObject, Database accordingTo);

    boolean equals(String otherObjectName, Database accordingTo);

}
