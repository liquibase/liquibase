package liquibase.change;

import liquibase.database.*;
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof DerbyDatabase) {
            throw new UnsupportedChangeException("Derby does not currently support dropping columns");
        } else if (database instanceof DB2Database) {
            return new SqlStatement[]{
                    new RawSqlStatement("ALTER TABLE " + getTableName() + " DROP COLUMN " + getColumnName()),
                    new RawSqlStatement("CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + getTableName() + "')")
            };
        } else if (database instanceof SybaseDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + getTableName() + " DROP " + getColumnName())};
        } else if (database instanceof FirebirdDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + getTableName() + " DROP " + getColumnName())};
        }
        return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + getTableName() + " DROP COLUMN " + getColumnName())};
    }

    public String getConfirmationMessage() {
        return "Column " + getTableName() + "." + getColumnName() + " dropped";
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
