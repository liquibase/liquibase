package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Modifies the data type of an existing column.
 */
public class ModifyColumnChange extends AbstractChange {

    private String tableName;
    private ColumnConfig column;

    public ModifyColumnChange() {
        super("modifyColumn", "Modify Column");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ColumnConfig getColumn() {
        return column;
    }

    public void setColumn(ColumnConfig column) {
        this.column = column;
    }

    public String[] generateStatements(MSSQLDatabase database) {
        return new String[]{"ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumn().getName() + " " + getColumn().getType()};
    }

    public String[] generateStatements(OracleDatabase database) {
        return new String[]{"ALTER TABLE " + getTableName() + " MODIFY (" + getColumn().getName() + " " + getColumn().getType() + ")"};
    }

    public String[] generateStatements(MySQLDatabase database) {
        return new String[]{"ALTER TABLE " + getTableName() + " MODIFY COLUMN " + getColumn().getName() + " " + getColumn().getType()};
    }

    public String[] generateStatements(PostgresDatabase database) {
        return new String[]{"ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumn().getName() + " TYPE " + getColumn().getType()};
    }

    public String getConfirmationMessage() {
        return "Column with the name " + column.getName() + " has been modified.";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("modifyColumn");
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentMigrationFileDOM));

        return node;
    }
}
