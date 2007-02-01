package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

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

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table " + getTableName());
        buffer.append(" add ");
        buffer.append(getColumn().getName() + " ");
        buffer.append(database.getColumnType(getColumn()));
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Column " + column.getName() + "("+column.getType()+") has been added to " + tableName;
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Table);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("addColumn");
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentMigrationFileDOM));

        return node;
    }
}
