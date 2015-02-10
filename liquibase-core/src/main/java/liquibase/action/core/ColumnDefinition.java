package liquibase.action.core;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.ObjectName;

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

    public ColumnDefinition(ObjectName columnName, String columnType) {
        set(Attr.columnName, columnName);
        set(Attr.columnType, columnType);
    }

    public ColumnDefinition(String columnName, String columnType) {
        this(new ObjectName(columnName), columnType);
    }

}
