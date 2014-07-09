package liquibase.change.core;

import liquibase.change.*;
import  liquibase.ExecutionEnvironment;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.Statement;
import liquibase.statement.core.DropIndexStatement;
import liquibase.structure.core.Index;

/**
 * Drops an existing index.
 */
@DatabaseChange(name="dropIndex", description = "Drops an existing index", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
public class DropIndexChange extends AbstractChange {

    private String schemaName;
    private String indexName;
    private String tableName;

    private String associatedWith;
    private String catalogName;

    @DatabaseChangeProperty(mustEqualExisting ="index.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to drop")
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name fo the indexed table.")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        return new Statement[] {
            new DropIndexStatement(getIndexName(), getCatalogName(), getSchemaName(), getTableName(), getAssociatedWith())
        };
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Index(getIndexName(), getCatalogName(), getSchemaName(), getTableName()), env.getTargetDatabase()), "Index exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table " + getTableName();
    }

    @DatabaseChangeProperty(isChangeProperty = false)
    public String getAssociatedWith() {
        return associatedWith;
    }

    public void setAssociatedWith(String associatedWith) {
        this.associatedWith = associatedWith;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
