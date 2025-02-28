package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropForeignKeyConstraintStatement;
import liquibase.structure.core.ForeignKey;
import lombok.Setter;

/**
 * Drops an existing foreign key constraint.
 */
@DatabaseChange(name = "dropForeignKeyConstraint", description = "Drops an existing foreign key", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "foreignKey")
@Setter
public class DropForeignKeyConstraintChange extends AbstractChange {
    private String baseTableCatalogName;
    private String baseTableSchemaName;
    private String baseTableName;
    private String constraintName;

    @DatabaseChangeProperty(mustEqualExisting ="foreignKey.table.catalog", since = "3.0",
        description = "Name of the database catalog of the base table")
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="foreignKey.table.schema",
        description = "Name of the database schema of the base table")
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "foreignKey.table",
        description = "Name of the table containing the column constrained by the foreign key")
    public String getBaseTableName() {
        return baseTableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "foreignKey", description = "Name of the foreign key constraint to drop",
        exampleValue = "fk_address_person")
    public String getConstraintName() {
        return constraintName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        return new SqlStatement[]{
                new DropForeignKeyConstraintStatement(
                        getBaseTableCatalogName(),
                        getBaseTableSchemaName(),
                        getBaseTableName(),
                        getConstraintName()),
        };    	
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new ForeignKey(getConstraintName(), getBaseTableCatalogName(), getBaseTableSchemaName(), getBaseTableCatalogName()), database), "Foreign key exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign key " + getConstraintName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
