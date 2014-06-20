package liquibase.structure;

import liquibase.ExtensibleObject;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.core.Schema;

public interface DatabaseObject extends Comparable, LiquibaseSerializable, ExtensibleObject {

    public String getSnapshotId();

    public void setSnapshotId(String id);

    DatabaseObject[] getContainingObjects();

    String getObjectTypeName();

    String getName();

    <T> T setName(String name);

    Schema getSchema();

    boolean snapshotByDefault();

}

