package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.Column;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

/**
 * This class is responsible for dropping a particular column in the specified table
 */
public class DropColumnChange extends AbstractChange {

    private String tableName;
    private String columnName;

    public DropColumnChange() {
        super("dropColumn", "Drop Column");
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ALTER TABLE ").append(getTableName());
        buffer.append(" DROP COLUMN ");
        buffer.append(getColumnName());
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Column " +getTableName()+"("+ getColumnName()+ ") dropped";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Column);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("dropColumn");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        return element;
    }
}
