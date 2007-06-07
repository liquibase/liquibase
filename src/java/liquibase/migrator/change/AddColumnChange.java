package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adds a column to an existing table.
 */
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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        return new String[]{"ALTER TABLE " + getTableName() + " ADD " + getColumn().getName() + " " + database.getColumnType(getColumn())};
    }

    protected Change[] createInverses() {
        DropColumnChange inverse = new DropColumnChange();
        inverse.setColumnName(getColumn().getName());
        inverse.setTableName(getTableName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Column " + column.getName() + "(" + column.getType() + ") has been added to " + tableName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("addColumn");
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentChangeLogFileDOM));

        return node;
    }
}
