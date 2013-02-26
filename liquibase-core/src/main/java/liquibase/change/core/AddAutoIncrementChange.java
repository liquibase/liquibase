package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.Column;

import java.math.BigInteger;

/**
 * Makes an existing column into an auto-increment column.
 * This change is only valid for databases with auto-increment/identity columns.
 * The current version does not support MS-SQL.
 */
@DatabaseChange(name="addAutoIncrement", description = "Converts an existing column to be an auto-increment (a.k.a 'identity') column",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column",
        databaseNotes = {@DatabaseChangeNote(database = "sqlite", notes = "If the column type is not INTEGER it is converted to INTEGER")}
)
public class AddAutoIncrementChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private BigInteger startWith;
    private BigInteger incrementBy;

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
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

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting ="column.relation")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting ="column")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Current data type fo the column to make auto-increment", exampleValue = "int")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @DatabaseChangeProperty(exampleValue = "100")
    public BigInteger getStartWith() {
    	return startWith;
    }
    
    public void setStartWith(BigInteger startWith) {
    	this.startWith = startWith;
    }

    @DatabaseChangeProperty(exampleValue = "1")
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
                    new CreateSequenceStatement(catalogName, schemaName, sequenceName),
                    new SetNullableStatement(catalogName, schemaName, getTableName(), getColumnName(), null, false),
                    new AddDefaultValueStatement(catalogName, schemaName, getTableName(), getColumnName(), getColumnDataType(), new SequenceNextValueFunction(sequenceName)),
            };
        }

        return new SqlStatement[]{new AddAutoIncrementStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), getStartWith(), getIncrementBy())};
    }

    public String getConfirmationMessage() {
        return "Auto-increment added to " + getTableName() + "." + getColumnName();
    }
}
