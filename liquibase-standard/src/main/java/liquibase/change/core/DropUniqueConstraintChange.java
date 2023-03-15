package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SybaseASADatabase;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropUniqueConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.UniqueConstraint;

/**
 * Removes an existing unique constraint.
 */
@DatabaseChange(
    name="dropUniqueConstraint",
    description = "Drops an existing unique constraint",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "uniqueConstraint"
)
public class DropUniqueConstraintChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;
    /**
     * Sybase ASA does drop unique constraint not by name, but using list of the columns in unique clause.
     */
    private String uniqueColumns;

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="uniqueConstraint.table.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="uniqueConstraint.table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(
        description = "Name of the table to drop the unique constraint from",
        mustEqualExisting = "uniqueConstraint.table"
    )
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty( description = "Name of unique constraint to drop", mustEqualExisting = "uniqueConstraint")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    @DatabaseChangeProperty(exampleValue = "name")
    public String getUniqueColumns() {
        return uniqueColumns;
    }

    public void setUniqueColumns(String uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
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
