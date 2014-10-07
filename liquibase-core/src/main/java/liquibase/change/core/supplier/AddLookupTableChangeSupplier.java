package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddLookupTableChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class AddLookupTableChangeSupplier extends AbstractChangeSupplier<AddLookupTableChange>  {

    public AddLookupTableChangeSupplier() {
        super(AddLookupTableChange.class);
    }

    @Override
    public Change[]  prepareDatabase(AddLookupTableChange change) throws DatabaseException {
        CreateTableChange createTableChange= new CreateTableChange();
        createTableChange.setCatalogName(change.getExistingTableCatalogName());
        createTableChange.setSchemaName(change.getExistingTableSchemaName());
        createTableChange.setTableName(change.getExistingTableName());

        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("int"));
        createTableChange.addColumn(new ColumnConfig().setName(change.getExistingColumnName()).setType(change.getNewColumnDataType()));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, AddLookupTableChange change) {
        Table newTable = diffResult.getUnexpectedObject(new Table(change.getNewTableCatalogName(), change.getNewTableSchemaName(), change.getNewTableName()));
        assertNotNull(newTable);
        assertEquals(1, newTable.getColumns().size());
        assertNotNull(newTable.getColumn(change.getNewColumnName()));
//todo        assertEquals(change.getNewColumnDataType(), newTable.getColumn(change.getNewColumnName()).getType().toString());
        assertEquals(change.getNewColumnName().toUpperCase(), newTable.getPrimaryKey().getColumnNames().toUpperCase());

        assertNotNull(diffResult.getUnexpectedObject(new Table(change.getNewTableCatalogName(), change.getNewTableSchemaName(), change.getNewTableName())));
        assertNotNull(new ForeignKey(change.getConstraintName(), change.getExistingTableCatalogName(), change.getExistingTableSchemaName(), change.getExistingTableName(), new Column(change.getExistingColumnName())));

    }
}
