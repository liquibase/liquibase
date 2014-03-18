package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateProcedureChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropProcedureChange;
import liquibase.change.core.DropTableChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class CreateProcedureChangeSupplier extends AbstractChangeSupplier<CreateProcedureChange>  {

    public CreateProcedureChangeSupplier() {
        super(CreateProcedureChange.class);
    }

    @Override
    public Change[] prepareDatabase(CreateProcedureChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName("customers");
        createTableChange.addColumn(new ColumnConfig().setName("id").setType("int").setConstraints(new ConstraintsConfig().setNullable(false).setPrimaryKey(true)).setAutoIncrement(true));
        createTableChange.addColumn(new ColumnConfig().setName("first_name").setType("varchar(50)"));
        createTableChange.addColumn(new ColumnConfig().setName("last_name").setType("varchar(50)"));

        return new Change[] {createTableChange };

    }

    @Override
    public void checkDiffResult(DiffResult diffResult, CreateProcedureChange change) throws Exception {
        //todo
    }

    @Override
    public Change[] revertDatabase(CreateProcedureChange change) throws Exception {
        DropTableChange dropTableChange = new DropTableChange();
        dropTableChange.setCatalogName(change.getCatalogName());
        dropTableChange.setSchemaName(change.getSchemaName());
        dropTableChange.setTableName("customers");

        DropProcedureChange dropProcedureChange = new DropProcedureChange();
        dropProcedureChange.setCatalogName(change.getCatalogName());
        dropProcedureChange.setSchemaName(change.getSchemaName());
        dropProcedureChange.setProcedureName("new_customer");

        return new Change[] {
                dropProcedureChange,
                dropTableChange
        };
    }
}
