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
        computed,
    }

    public ColumnDefinition() {
    }

    public ColumnDefinition(String columnName, String columnType) {
        set(Attr.columnName, columnName);
        set(Attr.columnType, columnType);
    }

}
