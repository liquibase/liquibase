package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.Column;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

/**
 * This class is responsible for renaming the columns in a particular table.
 */
public class RenameColumnChange extends AbstractChange {
    private String tableName;
    private String oldColumnName;
    private String newColumnName;

    public RenameColumnChange() {
        super("renameColumn", "Rename Column");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOldColumnName() {
        return oldColumnName;
    }

    public void setOldColumnName(String oldColumnName) {
        this.oldColumnName = oldColumnName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table " + getTableName());
        buffer.append(" rename column ");
        buffer.append(getOldColumnName() + " ");
        buffer.append(" to " + getNewColumnName());
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Column with the name " + oldColumnName + " has been renamed to " + newColumnName;
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Column);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("renameColumn");
        node.setAttribute("tableName", getTableName());
        node.setAttribute("oldColumnName", getOldColumnName());
        node.setAttribute("newColumnName", getNewColumnName());

        return node;
    }
}
