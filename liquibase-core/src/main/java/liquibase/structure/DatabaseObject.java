package liquibase.structure;

import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.core.Schema;

import java.util.Set;

public interface DatabaseObject extends Comparable, LiquibaseSerializable {

    public String getSnapshotId();

    public void setSnapshotId(String id);

    DatabaseObject[] getContainingObjects();

    String getObjectTypeName();

    String getName();

    <T> T setName(String name);

    Schema getSchema();

    boolean snapshotByDefault();

    Set<String> getAttributes();

    <T> T getAttribute(String attribute, Class<T> type);

    <T> T getAttribute(String attribute, T defaultValue);

    DatabaseObject setAttribute(String attribute, Object value);

}

