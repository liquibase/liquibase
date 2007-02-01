package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.Column;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

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

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table " + getTableName());
        buffer.append(" modify (");
        //buffer.append(" (");
        buffer.append(getColumn().getName() + " ");
        buffer.append(getColumn().getType());
        buffer.append(")");
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Column with the name " + column.getName() + " has been modified.";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Column);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("modifyColumn");
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentMigrationFileDOM));

        return node;
    }
}
