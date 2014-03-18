package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.CreateViewChange;
import liquibase.change.core.DropViewChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.View;

import static junit.framework.TestCase.assertNotNull;

public class DropViewChangeSupplier extends AbstractChangeSupplier<DropViewChange>  {

    public DropViewChangeSupplier() {
        super(DropViewChange.class);
    }

    @Override
    public Change[]  prepareDatabase(DropViewChange change) throws DatabaseException {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName("person");
        createTableChange.addColumn(new ColumnConfig().setName("id").setType("int"));
        createTableChange.addColumn(new ColumnConfig().setName("name").setType("varchar(20)"));

        CreateViewChange createViewChange = new CreateViewChange();
        createViewChange.setCatalogName(change.getCatalogName());
        createViewChange.setSchemaName(change.getSchemaName());
        createViewChange.setViewName(change.getViewName());
        createViewChange.setSelectQuery("select * from person");

        return new Change[] {createTableChange, createViewChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropViewChange change) {
        assertNotNull(diffResult.getMissingObject(new View(change.getCatalogName(), change.getSchemaName(), change.getViewName())));
    }
}
