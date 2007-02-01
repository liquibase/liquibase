package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public class RenameTableChange extends AbstractChange {
    private String oldTableName;
    private String newTableName;

    public RenameTableChange() {
        super("renameTable", "Rename Table");
    }

    public String getOldTableName() {
        return oldTableName;
    }

    public void setOldTableName(String oldTableName) {
        this.oldTableName = oldTableName;
    }

    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("rename " + getOldTableName() + " to " + getNewTableName());
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Table with the name " + oldTableName + " has been renamed to " + newTableName;
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Table);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("renameTable");
        element.setAttribute("oldTableName", getOldTableName());
        element.setAttribute("newTableName", getNewTableName());

        return element;
    }

    public void doRefactoring() {
        //To change body of created methods use File | Settings | File Templates.
    }
}
