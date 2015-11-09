package liquibase.change.core;

import liquibase.action.ActionStatus;
import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameTableStatement;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Renames an existing table.
 */
@DatabaseChange(name="renameTable", description = "Renames an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class RenameTableChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String oldTableName;

    private String newTableName;

    public RenameTableChange() {
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", description = "Name of the table to rename", exampleValue = "person")
    public String getOldTableName() {
        return oldTableName;
    }

    public void setOldTableName(String oldTableName) {
        this.oldTableName = oldTableName;
    }

    @DatabaseChangeProperty(description = "New name for the table", exampleValue = "employee")
    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(new RenameTableStatement(getCatalogName(), getSchemaName(), getOldTableName(), getNewTableName()));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public ActionStatus checkStatus(Database database) {
        try {
            ActionStatus actionStatus = new ActionStatus();
//            Table newTable = SnapshotGeneratorFactory.getInstance().createSnapshot(new ObjectReference(Table.class, new ObjectReference(getCatalogName(), getSchemaName()), getNewTableName()), database);
//            Table oldTable = SnapshotGeneratorFactory.getInstance().createSnapshot(new ObjectReference(Table.class, new ObjectReference(getCatalogName(), getSchemaName(), getOldTableName()), database);

//            if (newTable == null && oldTable == null) {
//                return actionStatus.unknown("Neither table exists");
//            }
//            if (newTable != null && oldTable != null) {
//                return actionStatus.unknown("Both tables exist");
//            }
//            actionStatus.assertApplied(newTable != null, "New table does not exist");

            return actionStatus;
        } catch (Exception e) {
            return new ActionStatus().unknown(e);
        }

    }

    @Override
    protected Change[] createInverses() {
        RenameTableChange inverse = new RenameTableChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setOldTableName(getNewTableName());
        inverse.setNewTableName(getOldTableName());

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Table " + oldTableName + " renamed to " + newTableName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
