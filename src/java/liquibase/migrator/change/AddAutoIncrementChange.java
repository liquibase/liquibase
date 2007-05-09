package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

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

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("MS-SQL does not support adding identities to existing tables");
    }

    public String[] generateStatements(OracleDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("Oracle does not support auto-increment columns");
    }

    public String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException {
        return new String[] {
                "ALTER TABLE "+getTableName()+" MODIFY "+getColumnName()+" "+getColumnDataType()+" AUTO_INCREMENT",
        };

    }

    public String[] generateStatements(PostgresDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("Oracle does not support auto-increment columns");
    }

    public String getConfirmationMessage() {
        return "Column Set as Auto-Increment";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("addAutoIncrement");
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnName", getColumnName());

        return node;
    }
}
