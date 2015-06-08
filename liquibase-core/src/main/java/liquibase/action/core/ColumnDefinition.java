package liquibase.action.core;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.ObjectName;

public class ColumnDefinition extends AbstractExtensibleObject {
    public ObjectName columnName;
    public String columnType;
    public Object defaultValue;
    public String remarks;
    public String addAfterColumn;
    public String addBeforeColumn;
    public Integer addAtPosition;
    public AutoIncrementDefinition autoIncrementDefinition;
    public Boolean isPrimaryKey;
    public Boolean isNullable;
    public AbstractExtensibleObject[] constraints;
    public Boolean computed;
    public Boolean descending;

    public ColumnDefinition() {
    }

    public ColumnDefinition(ObjectName columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public ColumnDefinition(String columnName, String columnType) {
        this(new ObjectName(columnName), columnType);
    }

    public ColumnDefinition(String columnName, String columnType, boolean isNullable) {
        this(columnName, columnType);
        this.isNullable = isNullable;
    }

}
