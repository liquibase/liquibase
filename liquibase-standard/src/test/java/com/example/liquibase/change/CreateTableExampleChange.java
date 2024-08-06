package com.example.liquibase.change;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@DatabaseChange(
    name = "createTableExample",
    description = "Used in unit tests",
    priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class CreateTableExampleChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private List<ColumnConfigExample> columns = new ArrayList<ColumnConfigExample>();
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

    public CreateTableExampleChange setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public CreateTableExampleChange setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public CreateTableExampleChange setColumns(List<ColumnConfigExample> columns) {
        this.columns = columns;
        return this;
    }

    public CreateTableExampleChange setPrimaryKey(PrimaryKeyConfig primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public CreateTableExampleChange setUniqueConstraints(List<UniqueConstraintConfig> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
        return this;
    }

    public CreateTableExampleChange setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
        return this;
    }
}
