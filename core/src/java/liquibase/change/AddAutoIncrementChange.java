package liquibase.change;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.statement.*;
import liquibase.util.StringUtils;

/**
 * Makes an existing column into an auto-increment column.
 * This change is only valid for databases with auto-increment/identity columns.
 * The current version does not support MS-SQL.
 */
public class AddAutoIncrementChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;

    public AddAutoIncrementChange() {
        super("addAutoIncrement", "Set Column as Auto-Increment");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

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

    public SqlStatement[] generateStatements(Database database) {
        if (database instanceof PostgresDatabase) {
            String sequenceName = (getTableName() + "_" + getColumnName() + "_seq").toLowerCase();
            return new SqlStatement[]{
                    new CreateSequenceStatement(schemaName, sequenceName),
                    new SetNullableStatement(schemaName, getTableName(), getColumnName(), null, false),
                    new AddDefaultValueStatement(schemaName, getTableName(), getColumnName(), getColumnDataType(), sequenceName),
            };
        }

        return new SqlStatement[]{new AddAutoIncrementStatement(getSchemaName(), getTableName(), getColumnName(), getColumnDataType())};
    }

    public String getConfirmationMessage() {
        return "Auto-increment added to " + getTableName() + "." + getColumnName();
    }
}
