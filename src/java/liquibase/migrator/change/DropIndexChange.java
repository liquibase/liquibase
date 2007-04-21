package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.Index;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public class DropIndexChange extends AbstractChange {

    private String indexName;
    private String tableName;

    public DropIndexChange() {
        super("dropIndex", "Drop Index");
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP INDEX ");
        buffer.append(getIndexName());
        buffer.append(" ON ");
        buffer.append(getTableName());
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table "+getTableName();
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Index);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("dropIndex");
        element.setAttribute("indexName", getIndexName());
        element.setAttribute("tableName", getTableName());

        return element;
    }
}
