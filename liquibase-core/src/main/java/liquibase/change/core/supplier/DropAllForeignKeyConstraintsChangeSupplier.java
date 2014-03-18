package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropAllForeignKeyConstraintsChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.ForeignKey;

import static junit.framework.TestCase.assertNotNull;

public class DropAllForeignKeyConstraintsChangeSupplier extends AbstractChangeSupplier<DropAllForeignKeyConstraintsChange> {

    public DropAllForeignKeyConstraintsChangeSupplier() {
        super(DropAllForeignKeyConstraintsChange.class);
    }

    @Override
    public Change[]  prepareDatabase(DropAllForeignKeyConstraintsChange change) throws DatabaseException {
        CreateTableChange createBaseTable = new CreateTableChange();
        createBaseTable.setCatalogName(change.getBaseTableCatalogName());
        createBaseTable.setSchemaName(change.getBaseTableSchemaName());
        createBaseTable.setTableName(change.getBaseTableName());
        createBaseTable.addColumn(new ColumnConfig().setName("id").setType("int").setConstraints(new ConstraintsConfig().setPrimaryKey(true)));
        createBaseTable.addColumn(new ColumnConfig().setName("customer_id").setType("int"));

        AddForeignKeyConstraintChange createFKChange = new AddForeignKeyConstraintChange();
        createFKChange.setBaseTableCatalogName(change.getBaseTableCatalogName());
        createFKChange.setBaseTableSchemaName(change.getBaseTableSchemaName());
        createFKChange.setBaseTableName(change.getBaseTableName());
        createFKChange.setBaseColumnNames("customer_id");

        createFKChange.setReferencedTableCatalogName(change.getBaseTableCatalogName());
        createFKChange.setReferencedTableSchemaName(change.getBaseTableSchemaName());
        createFKChange.setReferencedTableName(change.getBaseTableName());
        createFKChange.setReferencedColumnNames("id");
        createFKChange.setConstraintName("fk_test");

        return new Change[] {createBaseTable, createFKChange };

    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropAllForeignKeyConstraintsChange change) {
        assertNotNull(diffResult.getMissingObject(new ForeignKey("fk_test", change.getBaseTableCatalogName(), change.getBaseTableSchemaName(), change.getBaseTableName())));
    }
}
