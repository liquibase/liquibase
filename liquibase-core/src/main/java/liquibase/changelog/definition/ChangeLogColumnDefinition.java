package liquibase.changelog.definition;

import liquibase.datatype.LiquibaseDataType;

public class ChangeLogColumnDefinition {

    private final String columnName;
    private final LiquibaseDataType dataType;
    private final Object defaultValue;

    public ChangeLogColumnDefinition(String columnName, LiquibaseDataType dataType, Object defaultValue) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
    }

    public ChangeLogColumnDefinition(String columnName, LiquibaseDataType dataType) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.defaultValue = null;
    }

    public String getColumnName() {
        return columnName;
    }

    public LiquibaseDataType getDataType() {
        return dataType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
