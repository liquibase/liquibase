package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.RawSQLChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.statement.core.RawSqlStatement;

import static org.junit.Assert.assertTrue;

public class RawSQLChangeSupplier extends AbstractChangeSupplier<RawSQLChange>  {

    public RawSQLChangeSupplier() {
        super(RawSQLChange.class);
    }

    @Override
    public Change[]  prepareDatabase(RawSQLChange change) throws DatabaseException {
        CreateTableChange createTableChange = new CreateTableChange();
//        createTableChange.setCatalogName(change.getCatalogName());
//        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName("person");
        createTableChange.addColumn(new ColumnConfig().setName("name").setType("varchar(10)"));
        createTableChange.addColumn(new ColumnConfig().setName("address").setType("varchar(10)"));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, RawSQLChange change) throws Exception {
        //todo generic check

        int rows = ExecutorService.getInstance().getExecutor(diffResult.getComparisonSnapshot().getDatabase()).queryForInt(new RawSqlStatement("select count(*) from person"));
        assertTrue(rows > 0);

    }

}
