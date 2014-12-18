package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    @DatabaseChangeProperty(mustEqualExisting ="column.relation")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Current data type of the column to make auto-increment", exampleValue = "int")
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
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (database instanceof PostgresDatabase) {
            String sequenceName = (getTableName() + "_" + getColumnName() + "_seq").toLowerCase();
            return new SqlStatement[]{
                    new CreateSequenceStatement(catalogName, schemaName, sequenceName),
                    new SetNullableStatement(catalogName, schemaName, getTableName(), getColumnName(), null, false),
                    new AddDefaultValueStatement(catalogName, schemaName, getTableName(), getColumnName(), getColumnDataType(), new SequenceNextValueFunction((schemaName==null?"":schemaName+".")+sequenceName)),
            };
        }

        return new SqlStatement[]{new AddAutoIncrementStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), getStartWith(), getIncrementBy())};
    }

    @Override
    public String getConfirmationMessage() {
        return "Auto-increment added to " + getTableName() + "." + getColumnName();
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        Column example = new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName());
        try {
            Column column = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            if (column == null) return result.unknown("Column does not exist");


            result.assertComplete(column.isAutoIncrement(), "Column is not auto-increment");
            if (getStartWith() != null && column.getAutoIncrementInformation().getStartWith() != null) {
                result.assertCorrect(getStartWith().equals(column.getAutoIncrementInformation().getStartWith()), "startsWith incorrect");
            }

            if (getIncrementBy() != null && column.getAutoIncrementInformation().getIncrementBy() != null) {
                result.assertCorrect(getIncrementBy().equals(column.getAutoIncrementInformation().getIncrementBy()), "Increment by incorrect");
            }

            return result;
        } catch (Exception e) {
            return result.unknown(e);

        }


    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
