package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class AddNotNullConstraintChangeSupplier extends AbstractChangeSupplier<AddNotNullConstraintChange>  {

    public AddNotNullConstraintChangeSupplier() {
        super(AddNotNullConstraintChange.class);
    }

    @Override
    public Change[]  prepareDatabase(AddNotNullConstraintChange change) throws DatabaseException {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        String type = change.getColumnDataType();
        if (type == null) {
            type = "varchar(10)";
        }
        createTableChange.addColumn(new ColumnConfig().setName(change.getColumnName()).setType(type));
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("varchar(10)"));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, AddNotNullConstraintChange change) {
        ObjectDifferences diff = diffResult.getChangedObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getColumnName()));
        assertNotNull(diff);
        assertEquals(true, diff.getDifference("nullable").getReferenceValue());
        assertEquals(false, diff.getDifference("nullable").getComparedValue());
//        assertNull(diff.getDifference("type").toString(), diff.getDifference("type"));

    }
}

