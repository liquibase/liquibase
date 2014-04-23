package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class RenameColumnChangeSupplier extends AbstractChangeSupplier<RenameColumnChange>  {

    public RenameColumnChangeSupplier() {
        super(RenameColumnChange.class);
    }

    @Override
    public Change[]  prepareDatabase(RenameColumnChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("int"));
        String dataType = change.getColumnDataType();
        if (dataType == null) {
            dataType = "int";
        }
        createTableChange.addColumn(new ColumnConfig().setName(change.getOldColumnName()).setType(dataType));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, RenameColumnChange change) {
        Column oldColumn = diffResult.getMissingObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getOldColumnName()));
        Column newColumn = diffResult.getUnexpectedObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getNewColumnName()));

        assertNotNull(oldColumn);
        assertNotNull(newColumn);
        assertEquals(oldColumn.getType().toString(), newColumn.getType().toString());
    }
}
