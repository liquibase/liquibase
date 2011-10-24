package liquibase.change.core;

import java.math.BigInteger;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.util.StringUtils;

/**
 * Makes an existing column into an auto-increment column.
 * This change is only valid for databases with auto-increment/identity columns.
 * The current version does not support MS-SQL.
 */
@ChangeClass(name="addAutoIncrement", description = "Set Column as Auto-Increment", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class AddAutoIncrementChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private BigInteger startWith;
    private BigInteger incrementBy;

    @ChangeProperty(mustApplyTo ="column.table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo ="column.table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo ="column")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    public BigInteger getStartWith() {
    	return startWith;
    }
    
    public void setStartWith(BigInteger startWith) {
    	this.startWith = startWith;
    }
    
    public BigInteger getIncrementBy() {
    	return incrementBy;
    }
    
    public void setIncrementBy(BigInteger incrementBy) {
    	this.incrementBy = incrementBy;
    }
    
    public SqlStatement[] generateStatements(Database database) {
        if (database instanceof PostgresDatabase) {
            String sequenceName = (getTableName() + "_" + getColumnName() + "_seq").toLowerCase();
            return new SqlStatement[]{
                    new CreateSequenceStatement(schemaName, sequenceName),
                    new SetNullableStatement(schemaName, getTableName(), getColumnName(), null, false),
                    new AddDefaultValueStatement(schemaName, getTableName(), getColumnName(), getColumnDataType(), new DatabaseFunction("NEXTVAL('"+sequenceName+"')")),
            };
        }

        return new SqlStatement[]{new AddAutoIncrementStatement(getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), getStartWith(), getIncrementBy())};
    }

    public String getConfirmationMessage() {
        return "Auto-increment added to " + getTableName() + "." + getColumnName();
    }
}
