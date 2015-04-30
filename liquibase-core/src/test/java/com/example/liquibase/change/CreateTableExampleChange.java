package com.example.liquibase.change;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(
    name = "createTableExample",
    description = "Used in unit tests",
    priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class CreateTableExampleChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
    private PrimaryKeyConfig primaryKey;
    private List<UniqueConstraintConfig> uniqueConstraints = new ArrayList<UniqueConstraintConfig>();
    private BigDecimal decimalValue;

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return null;
    }

    @Override
    public String getConfirmationMessage() {
        return "Test Confirmation Message";
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public PrimaryKeyConfig getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKeyConfig primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<UniqueConstraintConfig> getUniqueConstraints() {
        return uniqueConstraints;
    }

    public void setUniqueConstraints(List<UniqueConstraintConfig> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }
}
