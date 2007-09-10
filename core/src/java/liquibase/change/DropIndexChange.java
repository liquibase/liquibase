package liquibase.change;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MySQLDatabase) {
            return new String[]{"DROP INDEX " + indexName + " ON " + tableName};
        } else if (database instanceof MSSQLDatabase) {
            return new String[]{"DROP INDEX " + tableName + "." + indexName};
        } else if (database instanceof OracleDatabase) {
            return new String[]{"DROP INDEX " + indexName};
        }

        return new String[]{"DROP INDEX " + indexName};
    }

    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table " + getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropIndex");
        element.setAttribute("indexName", getIndexName());
        element.setAttribute("tableName", getTableName());

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Index index = new Index();
        index.setTableName(tableName);
        index.setName(indexName);

        Table table= new Table();
        table.setName(tableName);

        return new HashSet<DatabaseObject>(Arrays.asList(index, table));
    }
    
}
