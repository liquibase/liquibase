package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public class DropTableChange extends AbstractChange {

    private String tableName;
    private Boolean cascadeConstraints;

    public DropTableChange() {
        super("dropTable", "Drop Table");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Boolean isCascadeConstraints() {
        return cascadeConstraints;
    }

    public void setCascadeConstraints(Boolean cascadeConstraints) {
        this.cascadeConstraints = cascadeConstraints;
    }

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP TABLE ").append(getTableName());
        if (isCascadeConstraints() != null && isCascadeConstraints()) {
            buffer.append(" CASCADE CONSTRAINTS");
        }
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Table " + tableName + " dropped";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Table);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("dropTable");
        element.setAttribute("tableName", getTableName());

        if (isCascadeConstraints() != null) {
            element.setAttribute("cascadeConstraints", isCascadeConstraints().toString());
        }

        return element;
    }
}
