package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameColumnStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import lombok.Setter;

/**
 * Renames an existing column.
 */
@DatabaseChange(
    name = "renameColumn",
    description = "Renames an existing column",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "column"
)
@Setter
public class RenameColumnChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String oldColumnName;
    private String newColumnName;
    private String columnDataType;
    private String remarks;

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting = "column.relation.catalog", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(
        description = "Name of the table containing the column to rename",
        mustEqualExisting = "column.relation"
    )
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(
        description = "Name of the existing column to rename",
        exampleValue = "name",
        mustEqualExisting = "column"
    )
    public String getOldColumnName() {
        return oldColumnName;
    }

    @DatabaseChangeProperty(description = "New name for the column", exampleValue = "full_name")
    public String getNewColumnName() {
        return newColumnName;
    }

    @DatabaseChangeProperty(description = "Data type of the column")
    public String getColumnDataType() {
        return columnDataType;
    }

    @DatabaseChangeProperty(description = "A brief descriptive comment written to the column metadata")
    public String getRemarks() {
        return remarks;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new RenameColumnStatement(
                getCatalogName(),
                getSchemaName(),
                getTableName(), getOldColumnName(), getNewColumnName(),
                getColumnDataType(),getRemarks())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            ChangeStatus changeStatus = new ChangeStatus();
            Column newColumn = SnapshotGeneratorFactory.getInstance().createSnapshot(
                new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getNewColumnName()),
                database
            );
            Column oldColumn = SnapshotGeneratorFactory.getInstance().createSnapshot(
                new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getOldColumnName()),
                database
            );

            if ((newColumn == null) && (oldColumn == null)) {
                return changeStatus.unknown("Neither column exists");
            }
            if ((newColumn != null) && (oldColumn != null)) {
                return changeStatus.unknown("Both columns exist");
            }
            changeStatus.assertComplete(newColumn != null, "New column does not exist");

            return changeStatus;
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    protected Change[] createInverses() {
        RenameColumnChange inverse = new RenameColumnChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setOldColumnName(getNewColumnName());
        inverse.setNewColumnName(getOldColumnName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Column "+tableName+"."+ oldColumnName + " renamed to " + newColumnName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
