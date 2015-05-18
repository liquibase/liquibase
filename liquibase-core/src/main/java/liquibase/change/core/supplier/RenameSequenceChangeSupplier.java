package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.core.CreateSequenceChange;
import liquibase.change.core.RenameSequenceChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class RenameSequenceChangeSupplier extends AbstractChangeSupplier<RenameSequenceChange>  {

    public RenameSequenceChangeSupplier() {
        super(RenameSequenceChange.class);
    }

    @Override
    public Change[]  prepareDatabase(RenameSequenceChange change) throws Exception {
        CreateSequenceChange createSequenceChange = new CreateSequenceChange();
        createSequenceChange.setCatalogName(change.getCatalogName());
        createSequenceChange.setSchemaName(change.getSchemaName());
        createSequenceChange.setSequenceName(change.getOldSequenceName());

        return new Change[] {createSequenceChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, RenameSequenceChange change) {
//        assertNotNull(diffResult.getMissingObject(new Table(change.getCatalogName(), change.getSchemaName(), change.getOldSequenceName())));
//        assertNotNull(diffResult.getUnexpectedObject(new Table(change.getCatalogName(), change.getSchemaName(), change.getNewSequenceName())));
    }
}
