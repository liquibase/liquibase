package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DropNotNullConstraintChangeSupplier extends AbstractChangeSupplier<DropNotNullConstraintChange>  {

    public DropNotNullConstraintChangeSupplier() {
        super(DropNotNullConstraintChange.class);
    }

    @Override
    public Change[]  prepareDatabase(DropNotNullConstraintChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        String columnType = change.getColumnDataType();
        if (columnType == null) {
            columnType = "int";
        }

        createTableChange.addColumn(new ColumnConfig().setName(change.getColumnName()).setType(columnType).setConstraints(new ConstraintsConfig().setNullable(false)));
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("int"));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropNotNullConstraintChange change) {
        ObjectDifferences diff = diffResult.getChangedObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getColumnName()));
        assertFalse((Boolean) diff.getDifference("nullable").getReferenceValue());
        assertTrue((Boolean) diff.getDifference("nullable").getComparedValue());
    }
}
