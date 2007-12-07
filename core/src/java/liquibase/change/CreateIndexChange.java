package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.CreateIndexStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Creates an index on an existing column.
 */
public class CreateIndexChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String indexName;
    private String tablespace;
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

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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


    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        List<String> columns = new ArrayList<String>();
        for (ColumnConfig column : getColumns()) {
            columns.add(column.getName());
        }

        return new SqlStatement []{
                new CreateIndexStatement(getIndexName(), getSchemaName(), getTableName(), columns.toArray(new String[getColumns().size()])).setTablespace(getTablespace())
        };
    }

    protected Change[] createInverses() {
        DropIndexChange inverse = new DropIndexChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setIndexName(getIndexName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " created";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("createIndex");
        element.setAttribute("indexName", getIndexName());
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("tableName", getTableName());

        for (ColumnConfig column : getColumns()) {
            Element columnElement = currentChangeLogFileDOM.createElement("column");
            columnElement.setAttribute("name", column.getName());
            element.appendChild(columnElement);
        }

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Index index = new Index();
        index.setTable(new Table(tableName));
        index.setName(indexName);

        Table table= new Table(getTableName());

        return new HashSet<DatabaseObject>(Arrays.asList(index, table));
    }

}
