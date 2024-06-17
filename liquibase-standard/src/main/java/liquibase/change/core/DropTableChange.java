package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.Table;
import lombok.Setter;

/**
 * Drops an existing table.
 */
@DatabaseChange(name = "dropTable", description = "Drops an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "table")
@Setter
public class DropTableChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private Boolean cascadeConstraints;

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", description = "Name of the table to drop")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(description = "Whether to add CASCADE CONSTRAINTS to the SQL statement")
    public Boolean isCascadeConstraints() {
        return cascadeConstraints;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        boolean constraints = false;
        if (isCascadeConstraints() != null) {
            constraints = isCascadeConstraints();
        }
        
        return new SqlStatement[]{
                new DropTableStatement(getCatalogName(), getSchemaName(), getTableName(), constraints)
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Table(getCatalogName(), getSchemaName(), getTableName()), database), "Table exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "Table " + getTableName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
