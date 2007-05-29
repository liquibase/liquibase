package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Removes the default value from an existing column.
 */
public class DropDefaultValueChange extends AbstractChange {
    private String tableName;
    private String columnName;

    public DropDefaultValueChange() {
        super("dropDefaultValue", "Drop Default Value");
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

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        //docs on how to at http://doc.ddart.net/mssql/sql70/de-dz_9.htm
        throw new UnsupportedChangeException("Dropping default values is not currently supported in MS-SQL");
    }

    public String[] generateStatements(MySQLDatabase database) {
        return new String[]{
                "ALTER TABLE " + getTableName() + " ALTER " + getColumnName() + " DROP DEFAULT",
        };
    }

    public String[] generateStatements(OracleDatabase database) {
        return new String[]{
                "ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " DEFAULT NULL",
        };
    }

    public String[] generateStatements(PostgresDatabase database) {
        return new String[]{
                "ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " SET DEFAULT NULL",
        };
    }

    public String getConfirmationMessage() {
        return "Default Value Dropped";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnName", getColumnName());

        return node;
    }
}
