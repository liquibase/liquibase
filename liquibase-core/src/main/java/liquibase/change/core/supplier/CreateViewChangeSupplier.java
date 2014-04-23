package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.CreateViewChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.View;

import static junit.framework.TestCase.assertNotNull;

public class CreateViewChangeSupplier extends AbstractChangeSupplier<CreateViewChange>  {

    public CreateViewChangeSupplier() {
        super(CreateViewChange.class);
    }

    @Override
    public Change[]  prepareDatabase(CreateViewChange change) throws Exception {

        CreateTableChange createTableChange = new CreateTableChange();
//        createTableChange.setCatalogName(change.getCatalogName());
//        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName("person");
        createTableChange.addColumn(new ColumnConfig().setName("id").setType("int"));
        createTableChange.addColumn(new ColumnConfig().setName("name").setType("varchar(255)"));

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, CreateViewChange change) {
        assertNotNull(diffResult.getUnexpectedObject(new View(change.getCatalogName(), change.getSchemaName(), change.getViewName())));
    }
}
