package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.database.struture.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class will handle the insert statements encountered in the xml file.
 * It will also execute the statements against the specified database or provide
 * the String representation of the insert refactoring for the specified database.
 */
public class InsertDataChange extends AbstractChange {

    private String tableName;
    private List<ColumnConfig> columns;

    public InsertDataChange() {
        super("insert", "Insert Row");
        columns = new ArrayList<ColumnConfig>();
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

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }


    public String generateStatement(AbstractDatabase database) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("insert into ").append(getTableName()).append(" ");
        Iterator iterator = columns.iterator();
        StringBuffer columnNames = new StringBuffer();
        StringBuffer columnValues = new StringBuffer();

        columnNames.append("(");
        columnValues.append("(");
        while (iterator.hasNext()) {
            ColumnConfig column = (ColumnConfig) iterator.next();
            columnNames.append(column.getName());
            columnValues.append("'");
            columnValues.append(column.getValue());
            columnValues.append("'");
            if (iterator.hasNext()) {
                columnNames.append(", ");
                columnValues.append(", ");
            }
        }
        columnNames.append(")");
        columnValues.append(")");
        buffer.append(columnNames);
        buffer.append(" values ");
        buffer.append(columnValues);
        return buffer.toString();
    }

    public String getConfirmationMessage() {
        return "New rows have been inserted into the table " + tableName;
    }

    public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
        return selectedDatabaseStructures.size() == 1 && (selectedDatabaseStructures.iterator().next() instanceof Table);
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("insert");
        node.setAttribute("tableName", getTableName() );

        for (ColumnConfig col : getColumns()) {
            Element subNode = col.createNode(currentMigrationFileDOM);
            node.appendChild(subNode);
        }
        return node;
    }
}
