package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.core.StopChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class StopChangeSupplier extends AbstractChangeSupplier<StopChange> {

    public StopChangeSupplier() {
        super(StopChange.class);
    }

    @Override
    public Change[] prepareDatabase(StopChange change) throws Exception {
        return new Change[0];
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, StopChange change) throws Exception {
        //todo
    }
}