package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.DropIndexStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Drops an existing index.
 */
public class DropIndexChange extends AbstractChange {

    private String schemaName;
    private String indexName;
    private String tableName;

    public DropIndexChange() {
        super("dropIndex", "Drop Index");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[] {
            new DropIndexStatement(getIndexName(), getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName())
        };
    }

    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table " + getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropIndex");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("indexName", getIndexName());
        element.setAttribute("tableName", getTableName());

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
