package liquibase.structure;

import liquibase.ExtensibleObject;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.core.Schema;

import java.util.Set;

public interface DatabaseObject extends Comparable, LiquibaseSerializable, ExtensibleObject {

    public String getSnapshotId();

    public void setSnapshotId(String id);

    DatabaseObject[] getContainingObjects();

    String getObjectTypeName();

    ObjectName getName();

    String getSimpleName();

    <T> T setName(ObjectName name);

    Schema getSchema();

    boolean snapshotByDefault();
}

