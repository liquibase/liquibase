package liquibase.action.core;

import liquibase.AbstractExtensibleObject;

public class ColumnDefinition extends AbstractExtensibleObject {
    public static enum Attr {
        columnName,
        columnType,
        defaultValue,
        remarks,
        addAfterColumn,
        addBeforeColumn,
        addAtPosition,
        autoIncrementDefinition,
        isPrimaryKey,
        isNullable,
        constraints,

    }
}
