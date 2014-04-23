package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.core.CreateSequenceChange;
import liquibase.change.core.DropSequenceChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Sequence;

import static junit.framework.TestCase.assertNotNull;

public class DropSequenceChangeSupplier extends AbstractChangeSupplier<DropSequenceChange>  {

    public DropSequenceChangeSupplier() {
        super(DropSequenceChange.class);
    }

    @Override
    public Change[]  prepareDatabase(DropSequenceChange change) throws DatabaseException {
        CreateSequenceChange createSequenceChange = new CreateSequenceChange();
        createSequenceChange.setCatalogName(change.getCatalogName());
        createSequenceChange.setSchemaName(change.getSchemaName());
        createSequenceChange.setSequenceName(change.getSequenceName());

        return new Change[] {createSequenceChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropSequenceChange change) {
        assertNotNull(diffResult.getMissingObject(new Sequence(change.getCatalogName(), change.getSchemaName(), change.getSequenceName())));
    }
}
