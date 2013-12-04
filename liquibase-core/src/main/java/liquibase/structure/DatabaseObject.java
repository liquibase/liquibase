package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.core.Schema;

import java.util.Set;
import java.util.UUID;

public interface DatabaseObject extends Comparable, LiquibaseSerializable {

    public UUID getSnapshotId();

    public void setSnapshotId(UUID id);

    DatabaseObject[] getContainingObjects();

    String getObjectTypeName();

    String getName();

    <T> T setName(String name);

    Schema getSchema();

    boolean snapshotByDefault();

    Set<String> getAttributes();

    <T> T getAttribute(String attribute, Class<T> type);

    DatabaseObject setAttribute(String attribute, Object value);

}

