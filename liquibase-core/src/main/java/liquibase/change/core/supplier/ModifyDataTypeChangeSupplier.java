package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import static junit.framework.TestCase.assertNotNull;

public class ModifyDataTypeChangeSupplier extends AbstractChangeSupplier<ModifyDataTypeChange>  {

    public ModifyDataTypeChangeSupplier() {
        super(ModifyDataTypeChange.class);
    }

    @Override
    public Change[]  prepareDatabase(ModifyDataTypeChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        String dataType = change.getNewDataType();
        if (dataType.startsWith("int")) {
            dataType = "varchar(20)";
        } else {
            dataType = "int";
        }
        createTableChange.addColumn(new ColumnConfig().setName(change.getColumnName()).setType(dataType));
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("varchar(10)"));

        return new Change[] {createTableChange };

    }

    @Override
    public void checkDiffResult(DiffResult diffResult, ModifyDataTypeChange change) {
        ObjectDifferences colDiff = diffResult.getChangedObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getColumnName()));
        assertNotNull(colDiff);
        assertNotNull(colDiff.getDifference("type"));
//todo        assertEquals(change.getNewDataType(), colDiff.getDifference("type").getComparedValue().toString());
    }
}
