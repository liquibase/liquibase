package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.MergeColumnChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import static junit.framework.TestCase.assertNotNull;

public class MergeColumnChangeSupplier extends AbstractChangeSupplier<MergeColumnChange>  {

    public MergeColumnChangeSupplier() {
        super(MergeColumnChange.class);
    }

    @Override
    public Change[]  prepareDatabase(MergeColumnChange change) throws DatabaseException {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        createTableChange.addColumn(new ColumnConfig().setName(change.getColumn1Name()).setType("varchar(10)"));
        createTableChange.addColumn(new ColumnConfig().setName(change.getColumn2Name()).setType("varchar(10)"));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, MergeColumnChange change) {
        assertNotNull(diffResult.getMissingObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getColumn1Name())));
        assertNotNull(diffResult.getMissingObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getColumn2Name())));

        assertNotNull(diffResult.getUnexpectedObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getFinalColumnName())));

    }
}
