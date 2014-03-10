package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.core.CreateSequenceChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Sequence;

import static junit.framework.Assert.assertNotNull;

public class CreateSequenceChangeSupplier extends AbstractChangeSupplier<CreateSequenceChange>  {

    public CreateSequenceChangeSupplier() {
        super(CreateSequenceChange.class);
    }

    @Override
    public Change[] prepareDatabase(CreateSequenceChange change) throws Exception {
        return null;
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, CreateSequenceChange change) throws Exception {
        assertNotNull(diffResult.getUnexpectedObject(new Sequence(change.getCatalogName(), change.getSchemaName(), change.getSequenceName())));
    }
}
