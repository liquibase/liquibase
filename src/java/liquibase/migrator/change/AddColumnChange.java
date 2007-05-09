package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import liquibase.migrator.RollbackImpossibleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AddColumnChange extends AbstractChange {

    private String tableName;
    private ColumnConfig column;

    public AddColumnChange() {
        super("addColumn", "Add Column");
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

    private String[] generateCommonStatements(AbstractDatabase database) {
        return new String[] { "ALTER TABLE " + getTableName() + " ADD " + getColumn().getName() + " " + database.getColumnType(getColumn()) };
    }

    public String[] generateStatements(MSSQLDatabase database) {
        return generateCommonStatements(((AbstractDatabase) database));
    }

    public String[] generateStatements(OracleDatabase database) {
        return generateCommonStatements(((AbstractDatabase) database));
    }

    public String[] generateStatements(MySQLDatabase database) {
        return generateCommonStatements(((AbstractDatabase) database));
    }

    public String[] generateStatements(PostgresDatabase database) {
        return generateCommonStatements(((AbstractDatabase) database));
    }

    protected AbstractChange createInverse() {
        DropColumnChange inverse = new DropColumnChange();
        inverse.setColumnName(getColumn().getName());
        inverse.setTableName(getTableName());

        return inverse;
    }

    public String getConfirmationMessage() {
        return "Column " + column.getName() + "(" + column.getType() + ") has been added to " + tableName;
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("addColumn");
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentMigrationFileDOM));

        return node;
    }
}
