package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.UniqueConstraint;

import static junit.framework.Assert.assertNotNull;

public class DropUniqueConstraintChangeSupplier extends AbstractChangeSupplier<DropUniqueConstraintChange>  {

    public DropUniqueConstraintChangeSupplier() {
        super(DropUniqueConstraintChange.class);
    }

    @Override
    public Change[]  prepareDatabase(DropUniqueConstraintChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        String uniqueColumns = change.getUniqueColumns();

        if (uniqueColumns == null) {
            uniqueColumns = "test_col";
        }

        for (String column : uniqueColumns.split(",")) {
            createTableChange.addColumn(new ColumnConfig().setName(column.trim()).setType("int").setConstraints(new ConstraintsConfig().setNullable(false)));
        }
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("int"));

        AddUniqueConstraintChange addUniqueConstraintChange = new AddUniqueConstraintChange();
        addUniqueConstraintChange.setCatalogName(change.getCatalogName());
        addUniqueConstraintChange.setSchemaName(change.getSchemaName());
        addUniqueConstraintChange.setTableName(change.getTableName());
        addUniqueConstraintChange.setColumnNames(uniqueColumns);
        addUniqueConstraintChange.setConstraintName(change.getConstraintName());

        return new Change[] {createTableChange, addUniqueConstraintChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropUniqueConstraintChange change) {
        Column[] columns = null;
        if (change.getUniqueColumns() != null) {
            String[] columnNames = change.getUniqueColumns().split(",");
            for (int i=0; i<columnNames.length; i++) {
                columns[i] = new Column(columnNames[i]);
            }
        }

        assertNotNull(diffResult.getMissingObject(new UniqueConstraint(change.getConstraintName(), change.getCatalogName(), change.getSchemaName(), change.getTableName(), columns)));
    }
}
