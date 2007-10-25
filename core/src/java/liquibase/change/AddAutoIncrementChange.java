package liquibase.change;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.sql.AddAutoIncrementStatement;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
        this.schemaName = schemaName;
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof PostgresDatabase) {
            String sequenceName = (getTableName()+"_"+getColumnName()+"_seq").toLowerCase();
            return new SqlStatement[]{
                    new RawSqlStatement("CREATE SEQUENCE "+sequenceName),
                    new RawSqlStatement("ALTER TABLE "+database.escapeTableName(getSchemaName(), getTableName())+" ALTER COLUMN "+getColumnName()+" SET NOT NULL"),
                    new RawSqlStatement("ALTER TABLE "+database.escapeTableName(getSchemaName(), getTableName())+" ALTER COLUMN "+getColumnName()+" SET DEFAULT nextval ('"+sequenceName+"')"),
            };
        } else {
            return new SqlStatement[] { new AddAutoIncrementStatement(getSchemaName(), getTableName(), getColumnName(), getColumnDataType())};
        }
    }

    public String getConfirmationMessage() {
        return "Auto-increment added to "+getTableName()+"."+getColumnName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("addAutoIncrement");
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnName", getColumnName());
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Column column = new Column();

        Table table = new Table(getTableName());
        column.setTable(table);

        column.setName(columnName);

        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }
    
}
