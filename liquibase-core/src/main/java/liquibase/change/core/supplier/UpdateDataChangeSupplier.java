package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class UpdateDataChangeSupplier extends AbstractChangeSupplier<UpdateDataChange>  {

    public UpdateDataChangeSupplier() {
        super(UpdateDataChange.class);
    }

    @Override
    public Change[]  prepareDatabase(UpdateDataChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());

        for (ColumnConfig column : change.getColumns()) {
            createTableChange.addColumn(new ColumnConfig().setName(column.getName()).setType("int"));
        }
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("int"));

        if (change.getWhere() != null) {
            String whereColumn = change.getWhere().replaceFirst("(.*?)=.*", "$1");
            createTableChange.addColumn(new ColumnConfig().setName(whereColumn).setType("varchar(50)"));
        }

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, UpdateDataChange change) throws Exception {
        //todo
    }
}
