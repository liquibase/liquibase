package liquibase.structure;

import liquibase.database.Database;
import liquibase.structure.core.Schema;

public interface DatabaseObject {
    DatabaseObject[] getContainingObjects();
    
    String getName();

    Schema getSchema();

    boolean equals(DatabaseObject otherObject, Database accordingTo);

    boolean equals(String otherObjectName, Database accordingTo);

}
