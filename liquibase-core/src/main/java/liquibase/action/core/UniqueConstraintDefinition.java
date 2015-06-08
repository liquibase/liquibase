package liquibase.action.core;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.ObjectName;

import java.util.List;

public class UniqueConstraintDefinition extends AbstractExtensibleObject {

    public String constraintName;
    public ObjectName tableName;
    public List<String> columnNames;
}
