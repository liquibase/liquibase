package liquibase.structure;

import liquibase.ExtensibleObject;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.core.Schema;

public interface DatabaseObject extends Comparable, LiquibaseSerializable, ExtensibleObject {

    String getSnapshotId();

    String getObjectTypeName();

    String getName();

    ObjectReference getContainer();

    boolean snapshotByDefault();

    ObjectReference toReference();
}

