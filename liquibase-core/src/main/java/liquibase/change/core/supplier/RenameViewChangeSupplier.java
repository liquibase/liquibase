package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.core.RenameViewChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.View;

import static junit.framework.Assert.assertNotNull;

public class RenameViewChangeSupplier extends AbstractChangeSupplier<RenameViewChange>  {

    public RenameViewChangeSupplier() {
        super(RenameViewChange.class);
    }

    @Override
    public Change[] prepareDatabase(RenameViewChange change) throws Exception {
        return null;
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, RenameViewChange change) throws Exception {
          assertNotNull(diffResult.getMissingObject(new View(change.getCatalogName(), change.getSchemaName(), change.getOldViewName())));
        assertNotNull(diffResult.getUnexpectedObject(new View(change.getCatalogName(), change.getSchemaName(), change.getNewViewName())));
    }
}
