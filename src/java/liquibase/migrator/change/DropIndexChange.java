package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    public String[] generateStatements(MSSQLDatabase database) {
        return new String[]{"DROP INDEX " + tableName + "." + indexName};
    }

    public String[] generateStatements(OracleDatabase database) {
        return new String[]{"DROP INDEX " + indexName};
    }

    public String[] generateStatements(MySQLDatabase database) {
        return new String[]{"DROP INDEX " + indexName + " ON " + tableName};
    }

    public String[] generateStatements(PostgresDatabase database) {
        return new String[]{"DROP INDEX " + indexName};
    }

    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table " + getTableName();
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("dropIndex");
        element.setAttribute("indexName", getIndexName());
        element.setAttribute("tableName", getTableName());

        return element;
    }
}
