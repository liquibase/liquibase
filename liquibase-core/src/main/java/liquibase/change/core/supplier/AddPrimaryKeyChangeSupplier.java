package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
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
        String[] columnNames = change.getColumnNames().split(",");
        Column[] columns = new Column[columnNames.length];
        for (int i=0; i<columnNames.length; i++) {
            columns[i] = new Column(columnNames[i]);
        }
        PrimaryKey pk = diffResult.getUnexpectedObject(new PrimaryKey(change.getConstraintName(), change.getCatalogName(), change.getSchemaName(), change.getTableName(), columns));
        assertNotNull(pk);
    }
}
