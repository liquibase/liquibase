package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Table;

import static junit.framework.Assert.assertNotNull;

public class CreateTableChangeSupplier extends AbstractChangeSupplier<CreateTableChange>  {

    public CreateTableChangeSupplier() {
        super(CreateTableChange.class);
    }

    @Override
    public Change[] prepareDatabase(CreateTableChange change) throws Exception {
        return null;
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, CreateTableChange change) throws Exception {
        assertNotNull(diffResult.getUnexpectedObject(new Table(change.getCatalogName(), change.getSchemaName(), change.getTableName())));
    }
}
