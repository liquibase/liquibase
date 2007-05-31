package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Creates an index on an existing column.
 */
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

    private String[] generateStatements() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE INDEX ");
        buffer.append(getIndexName()).append(" ON ");
        buffer.append(getTableName()).append("(");
        Iterator iterator = columns.iterator();
        while (iterator.hasNext()) {
            ColumnConfig column = (ColumnConfig) iterator.next();
            buffer.append(column.getName());
            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
        return new String []{buffer.toString()};
    }

    public String[] generateStatements(MSSQLDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(OracleDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(MySQLDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(PostgresDatabase database) {
        return generateStatements();
    }

    protected AbstractChange[] createInverses() {
        DropIndexChange inverse = new DropIndexChange();
        inverse.setTableName(getTableName());
        inverse.setIndexName(getIndexName());

        return new AbstractChange[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Index " + indexName + " has been created";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("createIndex");
        element.setAttribute("indexName", getIndexName());
        element.setAttribute("tableName", getTableName());

        for (ColumnConfig column : getColumns()) {
            Element columnElement = currentChangeLogFileDOM.createElement("column");
            columnElement.setAttribute("name", column.getName());
            element.appendChild(columnElement);
        }

        return element;
    }

}
