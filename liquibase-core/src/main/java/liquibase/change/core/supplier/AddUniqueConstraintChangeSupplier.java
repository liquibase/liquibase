package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.UniqueConstraint;

public class AddUniqueConstraintChangeSupplier extends AbstractChangeSupplier<AddUniqueConstraintChange>  {

    public AddUniqueConstraintChangeSupplier() {
        super(AddUniqueConstraintChange.class);
    }

    @Override
    public Change[]  prepareDatabase(AddUniqueConstraintChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());

        for (String column : change.getColumnNames().split(",")) {
            createTableChange.addColumn(new ColumnConfig().setName(column.trim()).setType("int").setConstraints(new ConstraintsConfig().setNullable(false)));
        }
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("int"));

        return new Change[] {createTableChange };

    }

    @Override
    public void checkDiffResult(DiffResult diffResult, AddUniqueConstraintChange change) {
        diffResult.getUnexpectedObject(new UniqueConstraint(change.getConstraintName(), change.getCatalogName(), change.getSchemaName(), change.getTableName()));
    }
}