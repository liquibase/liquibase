package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;
import liquibase.structure.core.Index;

/**
 * Drops an existing index.
 */
@DatabaseChange(name="dropIndex", description = "Drop Index", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
public class DropIndexChange extends AbstractChange {

    private String schemaName;
    private String indexName;
    private String tableName;

    @DatabaseChangeProperty(includeInSerialization = false)
    private String associatedWith;
    private String catalogName;

    @DatabaseChangeProperty(mustEqualExisting ="index.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "index")
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "index.table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
            new DropIndexStatement(getIndexName(), getCatalogName(), getSchemaName(), getTableName(), getAssociatedWith())
        };
    }

    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table " + getTableName();
    }

    public String getAssociatedWith() {
        return associatedWith;
    }

    public void setAssociatedWith(String associatedWith) {
        this.associatedWith = associatedWith;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
}
