package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;

/**
 * Drops an existing index.
 */
@ChangeClass(name="dropIndex", description = "Drop Index", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
public class DropIndexChange extends AbstractChange {

    private String schemaName;
    private String indexName;
    private String tableName;

    @ChangeProperty(includeInSerialization = false)
    private String associatedWith;
    private String catalogName;

    @ChangeProperty(mustApplyTo ="index.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "index")
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "index.table")
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
