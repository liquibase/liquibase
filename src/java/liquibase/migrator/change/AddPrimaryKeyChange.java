package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class AddPrimaryKeyChange extends AbstractChange {

    private String tableName;
    private String columnNames;

    public AddPrimaryKeyChange() {
        super("addPrimaryKey", "Add Primary Key");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    private String[] generateCommonStatements(AbstractDatabase database) {
        return new String[] {
                "ALTER TABLE "+getTableName()+" ADD PRIMARY KEY ("+getColumnNames()+")",
        };
    }

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements(database);
    }

    public String[] generateStatements(OracleDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements(database);
    }

    public String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements(database);
    }

    public String[] generateStatements(PostgresDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements(database);
    }

    public String getConfirmationMessage() {
        return "Primary Key Added";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnNames", getColumnNames());

        return node;
    }
}
