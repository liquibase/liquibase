package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.Index;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CreateIndexChange extends AbstractChange {

    private String tableName;
    private String indexName;
    private List<ColumnConfig> columns;


    public CreateIndexChange() {
        super("createIndex", "Create Index");
        columns = new ArrayList<ColumnConfig>();
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

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE INDEX ");
        buffer.append(getIndexName()).append(" ON ");
        buffer.append(getTableName()).append("(");
        Iterator iterator = columns.iterator();
        while (iterator.hasNext()) {
            ColumnConfig column  = (ColumnConfig) iterator.next();
            buffer.append(column.getName());
            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "Index " + indexName + " has been created";
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Index);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("createIndex");
        element.setAttribute("indexName", getIndexName());
        element.setAttribute("tableName", getTableName());

        for (ColumnConfig column : getColumns()) {
            Element columnElement = currentMigrationFileDOM.createElement("column");
            columnElement.setAttribute("name", column.getName());
            element.appendChild(columnElement);
        }

        return element;
    }

}
