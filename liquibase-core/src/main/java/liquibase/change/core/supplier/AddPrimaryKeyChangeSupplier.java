package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.PrimaryKey;

import static junit.framework.Assert.assertNotNull;

public class AddPrimaryKeyChangeSupplier extends AbstractChangeSupplier<AddPrimaryKeyChange>  {

    public AddPrimaryKeyChangeSupplier() {
        super(AddPrimaryKeyChange.class);
    }

    @Override
    public Change[]  prepareDatabase(AddPrimaryKeyChange change) throws DatabaseException {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        for (String columnName : change.getColumnNames().split(",")) {
            createTableChange.addColumn(new ColumnConfig().setName(columnName.trim()).setType("int").setConstraints(new ConstraintsConfig().setNullable(false)));
        }
        createTableChange.addColumn(new ColumnConfig().setName("not_id").setType("int"));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, AddPrimaryKeyChange change) {
        PrimaryKey pk = diffResult.getUnexpectedObject(new PrimaryKey(change.getConstraintName(), change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getColumnNames().split(",")));
        assertNotNull(pk);
    }
}
