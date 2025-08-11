package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SybaseASADatabase;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropUniqueConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.UniqueConstraint;
import lombok.Setter;

/**
 * Removes an existing unique constraint.
 */
@DatabaseChange(
    name = "dropUniqueConstraint",
    description = "Drops an existing unique constraint",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "uniqueConstraint"
)
@Setter
public class DropUniqueConstraintChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;
    /**
     * SAP SQL Anywhere (Sybase ASA) drops unique constraint not by name, but using list of the columns in UNIQUE clause.
     */
    private String uniqueColumns;

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="uniqueConstraint.table.catalog",
        description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="uniqueConstraint.table.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(
        description = "Name of the table to drop the unique constraint from",
        mustEqualExisting = "uniqueConstraint.table"
    )
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(description = "Name of the unique constraint to drop", mustEqualExisting = "uniqueConstraint")
    public String getConstraintName() {
        return constraintName;
    }

    @DatabaseChangeProperty(exampleValue = "name",
        description = "For SAP SQL Anywhere, a list of columns in the UNIQUE clause",
        supportsDatabase = "sybase")
    public String getUniqueColumns() {
        return uniqueColumns;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        
        DropUniqueConstraintStatement statement =
            new DropUniqueConstraintStatement(getCatalogName(), getSchemaName(), getTableName(), getConstraintName());
        if (database instanceof SybaseASADatabase) {
            statement.setUniqueColumns(ColumnConfig.arrayFromNames(uniqueColumns));
        }
        return new SqlStatement[]{
            statement
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            UniqueConstraint example =
                new UniqueConstraint(getConstraintName(), getCatalogName(), getSchemaName(), getTableName());
            if (getUniqueColumns() != null) {
                for (String column : getUniqueColumns().split("\\s*,\\s*")) {
                    example.addColumn(example.getColumns().size(), new Column(column));
                }
            }
            return new ChangeStatus().assertComplete(
                !SnapshotGeneratorFactory.getInstance().has(example, database),
                "Unique constraint exists"
            );
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Unique constraint "+getConstraintName()+" dropped from "+getTableName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
