package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import liquibase.migrator.RollbackImpossibleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    private String[] generateStatements() {
        return new String[] { "ALTER TABLE " + getTableName() + " MODIFY (" + getColumn().getName() + " " + getColumn().getType() + ")" };
    }

    public String[] generateStatements(MSSQLDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(OracleDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(MySQLDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(PostgresDatabase database) {
        return generateStatements();
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
