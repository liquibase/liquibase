package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateProcedureChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropProcedureChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.StoredProcedure;

import static junit.framework.TestCase.assertNotNull;

public class DropProcedureChangeSupplier extends AbstractChangeSupplier<DropProcedureChange> {

    public DropProcedureChangeSupplier() {
        super(DropProcedureChange.class);
    }

    @Override
    public Change[]  prepareDatabase(DropProcedureChange change) throws DatabaseException {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName("customers");
        createTableChange.addColumn(new ColumnConfig().setName("id").setType("int").setConstraints(new ConstraintsConfig().setNullable(false).setPrimaryKey(true)).setAutoIncrement(true));
        createTableChange.addColumn(new ColumnConfig().setName("first_name").setType("varchar(50)"));
        createTableChange.addColumn(new ColumnConfig().setName("last_name").setType("varchar(50)"));

        CreateProcedureChange createProcedureChange = new CreateProcedureChange();
        createProcedureChange.setCatalogName(change.getCatalogName());
        createProcedureChange.setSchemaName(change.getSchemaName());
        createProcedureChange.setProcedureName(change.getProcedureName());
        createProcedureChange.setProcedureText("CREATE PROCEDURE new_customer(firstname VARCHAR(50), lastname VARCHAR(50))\n" +
                "   MODIFIES SQL DATA\n" +
                "   INSERT INTO CUSTOMERS (first_name, last_name) VALUES (firstname, lastname)");

        return new Change[] {
                createTableChange,
                createProcedureChange
        };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropProcedureChange change) {
        assertNotNull(diffResult.getMissingObject(new StoredProcedure(change.getCatalogName(), change.getSchemaName(), change.getProcedureName())));
    }

}
