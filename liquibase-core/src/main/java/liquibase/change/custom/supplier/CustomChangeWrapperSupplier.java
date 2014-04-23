package liquibase.change.custom.supplier;

import liquibase.change.Change;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class CustomChangeWrapperSupplier extends AbstractChangeSupplier<CustomChangeWrapper> {

    public CustomChangeWrapperSupplier() {
        super(CustomChangeWrapper.class);
    }

    @Override
    public Change[] prepareDatabase(CustomChangeWrapper change) throws Exception {

        return new Change[0];
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, CustomChangeWrapper change) {
    }
}

