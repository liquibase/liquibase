package liquibase.action.core;

import liquibase.AbstractExtensibleObject;

public class UniqueConstraintDefinition extends AbstractExtensibleObject {

    public static enum Attr {
        constraintName,
        columnNames
    }
}
