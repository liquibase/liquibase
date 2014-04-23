package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.PrimaryKey;

import static junit.framework.TestCase.assertNotNull;

public class DropPrimaryKeyChangeSupplier extends AbstractChangeSupplier<DropPrimaryKeyChange>  {

    public DropPrimaryKeyChangeSupplier() {
        super(DropPrimaryKeyChange.class);
    }

    @Override
    public Change[]  prepareDatabase(DropPrimaryKeyChange change) throws DatabaseException {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        createTableChange.addColumn(new ColumnConfig().setName("id").setType("int").setConstraints(new ConstraintsConfig().setNullable(false).setPrimaryKey(true).setPrimaryKeyName(change.getConstraintName())));
        createTableChange.addColumn(new ColumnConfig().setName("not_id").setType("int"));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropPrimaryKeyChange change) {
        assertNotNull(diffResult.getMissingObject(new PrimaryKey(change.getConstraintName(), change.getCatalogName(), change.getSchemaName(), change.getTableName())));
    }
}
