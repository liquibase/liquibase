package liquibase.changelog.definition;

import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.ColumnConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChangeLogColumnDefinition {

    private final String columnName;
    private final LiquibaseDataType dataType;
    private final Object defaultValue;
    private final List<ColumnConstraint> constraints;
    private final String remarks;

    public ChangeLogColumnDefinition(String columnName, LiquibaseDataType dataType, Object defaultValue, List<ColumnConstraint> constraints, String remarks) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.constraints = constraints;
        this.remarks = remarks;
    }

    public ChangeLogColumnDefinition(String columnName, LiquibaseDataType dataType, Object defaultValue, ColumnConstraint... constraints) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.constraints = Arrays.asList(constraints);
        this.remarks = null;
    }

    public ChangeLogColumnDefinition(String columnName, LiquibaseDataType dataType, Object defaultValue) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.constraints = Collections.emptyList();
        this.remarks = null;
    }

    public ChangeLogColumnDefinition(String columnName, LiquibaseDataType dataType) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.defaultValue = null;
        this.constraints = Collections.emptyList();
        this.remarks = null;
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

    public List<ColumnConstraint> getConstraints() {
        return constraints;
    }

    public String getRemarks() {
        return remarks;
    }
}
