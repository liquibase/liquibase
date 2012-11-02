package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.structure.core.Schema;

import java.util.UUID;

public interface DatabaseObject extends Comparable{

    public UUID getSnapshotId();

    public void setSnapshotId(UUID id);

    DatabaseObject[] getContainingObjects();
    
    String getName();

    Schema getSchema();

    boolean equals(DatabaseObject otherObject, Database accordingTo);

    boolean matches(DatabaseObject otherObject, Database accordingTo);

    boolean snapshotByDefault();

}
