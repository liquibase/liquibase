package liquibase.change.core.supplier;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropIndexChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Index;

import static junit.framework.TestCase.assertNotNull;

public class DropIndexChangeSupplier extends AbstractChangeSupplier<DropIndexChange>  {

    public DropIndexChangeSupplier() {
        super(DropIndexChange.class);
    }


    @Override
    public Change[]  prepareDatabase(DropIndexChange change) throws Exception {
        String usedTableName = change.getTableName();

        if (usedTableName == null) {
            usedTableName = "person";
        }

        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(usedTableName);

        createTableChange.addColumn(new ColumnConfig().setName("name").setType("varchar(255)").setConstraints(new ConstraintsConfig().setNullable(false)));
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("int"));

        CreateIndexChange createIndexChange = new CreateIndexChange();
        createIndexChange.setIndexName(change.getIndexName());
        createIndexChange.setTableName(usedTableName);
        createIndexChange.addColumn((AddColumnConfig) new AddColumnConfig().setName("name"));

        return new Change[] {createTableChange, createIndexChange };

    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropIndexChange change) {
        assertNotNull(diffResult.getMissingObject(new Index(change.getIndexName())));
    }
}
