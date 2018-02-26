package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a not-null constraint to an existing column.
 */
@DatabaseChange(name="addNotNullConstraint",
                description = "Adds a not-null constraint to an existing table. If a defaultNullValue attribute is " +
                "passed, all null values for the column will be updated to the passed value before the constraint " +
                "is applied.",
                priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class AddNotNullConstraintChange extends AbstractChange {

    class SQLiteAlterTableVisitor implements AlterTableVisitor {
        @Override
        public ColumnConfig[] getColumnsToAdd() {
            return new ColumnConfig[0];
        }
    
        @Override
        public boolean copyThisColumn(ColumnConfig column) {
            return true;
        }
    
        @Override
        public boolean createThisColumn(ColumnConfig column) {
            if (column.getName().equals(getColumnName())) {
                column.getConstraints().setNullable(false);
            }
            return true;
        }
    
        @Override
        public boolean createThisIndex(Index index) {
            return true;
        }
    
    }


    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String defaultNullValue;
    private String columnDataType;
    private String constraintName;
    
    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="column.relation.catalog")
    public String getCatalogName() {
        return catalogName;
    }
    
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }
    
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    
    @DatabaseChangeProperty(description = "Adds a not-null constraint to an " +
     "existing table. If a defaultNullValue attribute is passed, all null values for the column will be updated to " +
     "the passed value before the constraint is applied.", mustEqualExisting = "column.relation")
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    @DatabaseChangeProperty(description = "Name of the column to add the constraint to",
        mustEqualExisting = "column.relation.column")
    public String getColumnName() {
        return columnName;
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    @DatabaseChangeProperty(description = "Value to set all currently null values to. If not set, change will fail " +
     "if null values exist")
    public String getDefaultNullValue() {
        return defaultNullValue;
    }
    
    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }
    
    @DatabaseChangeProperty(description = "Current data type of the column")
    public String getColumnDataType() {
        return columnDataType;
    }
    
    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }
    
    @DatabaseChangeProperty(description = "Created constraint name (if database supports names for NOT NULL " +
     "constraints)")
    public String getConstraintName() {
        return constraintName;
    }
    
    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<>();
        
        if (defaultNullValue != null) {
            statements.add(new UpdateStatement(getCatalogName(), getSchemaName(), getTableName())
                                   .addNewColumnValue(getColumnName(), defaultNullValue)
                                   .setWhereClause(database.escapeObjectName(getColumnName(), Column.class) +
                                   " IS NULL"));
        }
        
        statements.add(new SetNullableStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(),
            getColumnDataType(), false, getConstraintName()));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
        }
        
        return statements.toArray(new SqlStatement[statements.size()]);
    }
    
    @Override
    protected Change[] createInverses() {
        DropNotNullConstraintChange inverse = new DropNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());
        
        return new Change[]{
                inverse
        };
    }
    
    @Override
    public String getConfirmationMessage() {
        return "NOT NULL constraint " +
            (StringUtils.trimToNull(getConstraintName()) != null
                ? String.format("\"%s\" ", getConstraintName()) : "" ) +
            "has been added to " + getTableName() + "." + getColumnName();
    }
    
    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}

