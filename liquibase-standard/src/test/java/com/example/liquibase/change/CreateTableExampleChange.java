package com.example.liquibase.change;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public CreateTableExampleChange setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public CreateTableExampleChange setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public CreateTableExampleChange setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
        return this;
    }

    public PrimaryKeyConfig getPrimaryKey() {
        return primaryKey;
    }

    public CreateTableExampleChange setPrimaryKey(PrimaryKeyConfig primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public List<UniqueConstraintConfig> getUniqueConstraints() {
        return uniqueConstraints;
    }

    public CreateTableExampleChange setUniqueConstraints(List<UniqueConstraintConfig> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
        return this;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public CreateTableExampleChange setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
        return this;
    }
}
