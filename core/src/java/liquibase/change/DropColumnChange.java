package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.SybaseDatabase;
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
 * Drops an existing column from a table.
 */
public class DropColumnChange extends AbstractChange {

    private String tableName;
    private String columnName;

    public DropColumnChange() {
        super("dropColumn", "Drop Column");
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof DerbyDatabase) {
            throw new UnsupportedChangeException("Derby does not currently support dropping columns");
        } else if (database instanceof DB2Database) {
            return new String[]{
                    "ALTER TABLE " + getTableName() + " DROP COLUMN " + getColumnName(),
                    "CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + getTableName() + "')"
            };
        } else if (database instanceof SybaseDatabase) {
            return new String[]{"ALTER TABLE " + getTableName() + " DROP " + getColumnName()};            
        }
        return new String[]{"ALTER TABLE " + getTableName() + " DROP COLUMN " + getColumnName()};
    }

    public String getConfirmationMessage() {
        return "Column " + getTableName() + "(" + getColumnName() + ") dropped";
    }

    public Element createNode
            (Document
                    currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropColumn");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {


        Table table = new Table();
        table.setName(tableName);

        Column column = new Column();
        column.setTable(table);
        column.setName(columnName);

        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }

}
