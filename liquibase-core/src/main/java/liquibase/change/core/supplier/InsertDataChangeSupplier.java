package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.InsertDataChange;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.executor.ExecutorService;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.statement.core.RawSqlStatement;

import static org.junit.Assert.assertTrue;

public class InsertDataChangeSupplier extends AbstractChangeSupplier<InsertDataChange>  {

    public InsertDataChangeSupplier() {
        super(InsertDataChange.class);
    }

    @Override
    public Change[]  prepareDatabase(InsertDataChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        for (ColumnConfig column : change.getColumns()) {
            createTableChange.addColumn(column);
        }

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, InsertDataChange change) throws Exception {
        Database database = diffResult.getComparisonSnapshot().getDatabase();
        int rows = ExecutorService.getInstance().getExecutor(database).queryForInt(new RawSqlStatement("select count(*) from " + database.escapeTableName(change.getCatalogName(), change.getSchemaName(), change.getTableName())));
        assertTrue(rows > 0);
    }
}
