package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Makes an existing column into an auto-increment column.
 * This change is only valid for databases with auto-increment/identity columns.
 * The current version does not support MS-SQL.
 */
public class AddAutoIncrementChange extends AbstractChange {

    private String tableName;
    private String columnName;
    private String columnDataType;

    public AddAutoIncrementChange() {
        super("addAutoIncrement", "Set Column as Auto-Increment");
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

    public String[] generateStatements(AbstractDatabase database) throws UnsupportedChangeException {
        if (database instanceof OracleDatabase) {
            throw new UnsupportedChangeException("Oracle does not support auto-increment columns");
        } else if (database instanceof MSSQLDatabase) {
            throw new UnsupportedChangeException("MS-SQL does not support adding identities to existing tables");
        } else if (database instanceof PostgresDatabase) {
            throw new UnsupportedChangeException("Oracle does not support auto-increment columns");
        }

        return new String[]{
                "ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " " + getColumnDataType() + " AUTO_INCREMENT",
        };
    }

    public String getConfirmationMessage() {
        return "Column Set as Auto-Increment";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("addAutoIncrement");
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnName", getColumnName());

        return node;
    }
}
