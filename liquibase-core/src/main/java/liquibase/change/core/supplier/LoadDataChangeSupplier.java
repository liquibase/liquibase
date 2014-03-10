package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.LoadDataChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class LoadDataChangeSupplier extends AbstractChangeSupplier<LoadDataChange>  {

    public LoadDataChangeSupplier() {
        super(LoadDataChange.class);
    }

    @Override
    public Change[]  prepareDatabase(LoadDataChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        for (ColumnConfig column : change.getColumns()) {
            createTableChange.addColumn(column);
        }
        for (String columnName : change.getCSVReader().readNext()) {
            createTableChange.addColumn(new ColumnConfig().setName(columnName).setType("varchar(20)"));
        }

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, LoadDataChange change) throws Exception {
        //todo
    }
}
