package liquibase.change.core;

import static liquibase.change.ChangeParameterMetaData.ALL;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;
import liquibase.structure.core.Index;
import lombok.Setter;

/**
 * Drops an existing index.
 */
@DatabaseChange(name = "dropIndex", description = "Drops an existing index", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
@Setter
public class DropIndexChange extends AbstractChange {

    private String schemaName;
    private String indexName;
    private String tableName;

    private String associatedWith;
    private String catalogName;

    @DatabaseChangeProperty(mustEqualExisting ="index.schema", description = "Name of the database schema", supportsDatabase = ALL)
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to drop", supportsDatabase = ALL)
    public String getIndexName() {
        return indexName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name of the indexed table",
        requiredForDatabase = { "sybase","mysql","mssql","mariadb", "asany" },
        supportsDatabase = ALL)
    public String getTableName() {
        return tableName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
            new DropIndexStatement(getIndexName(), getCatalogName(), getSchemaName(), getTableName(), getAssociatedWith())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Index(getIndexName(), getCatalogName(), getSchemaName(), getTableName()), database), "Index exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table " + getTableName();
    }

    @DatabaseChangeProperty(isChangeProperty = false,
        description = "Index associations. Valid values: primaryKey, foreignKey, uniqueConstriant, none")
    public String getAssociatedWith() {
        return associatedWith;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.catalog", since = "3.0", description = "Name of the database catalog",
        supportsDatabase = ALL)
    public String getCatalogName() {
        return catalogName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
