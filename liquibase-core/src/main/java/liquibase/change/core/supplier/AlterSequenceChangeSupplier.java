package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.core.AlterSequenceChange;
import liquibase.change.core.CreateSequenceChange;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Sequence;

import static junit.framework.Assert.assertEquals;

public class AlterSequenceChangeSupplier extends AbstractChangeSupplier<AlterSequenceChange>  {

    public AlterSequenceChangeSupplier() {
        super(AlterSequenceChange.class);
    }

    @Override
    public Change[]  prepareDatabase(AlterSequenceChange change) throws DatabaseException {
        CreateSequenceChange createSequenceChange = new CreateSequenceChange();
        createSequenceChange.setCatalogName(change.getCatalogName());
        createSequenceChange.setSchemaName(change.getSchemaName());
        createSequenceChange.setSequenceName(change.getSequenceName());

        return new Change[] {createSequenceChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, AlterSequenceChange change) {
        if (change.getMinValue() != null) {
            ObjectDifferences diff = diffResult.getChangedObject(new Sequence(change.getCatalogName(), change.getSchemaName(), change.getSequenceName()));
            assertEquals(change.getMinValue(), diff.getDifference("minValue").getComparedValue());
        }

        if (change.getMaxValue() != null) {
            ObjectDifferences diff = diffResult.getChangedObject(new Sequence(change.getCatalogName(), change.getSchemaName(), change.getSequenceName()));
            assertEquals(change.getMaxValue(), diff.getDifference("maxValue").getComparedValue());
        }

        if (change.getIncrementBy() != null) {
            ObjectDifferences diff = diffResult.getChangedObject(new Sequence(change.getCatalogName(), change.getSchemaName(), change.getSequenceName()));
            assertEquals(change.getIncrementBy(), diff.getDifference("incrementBy").getComparedValue());
        }

        if (change.isOrdered() != null) {
            ObjectDifferences diff = diffResult.getChangedObject(new Sequence(change.getCatalogName(), change.getSchemaName(), change.getSequenceName()));
            assertEquals(change.isOrdered(), diff.getDifference("ordered").getComparedValue());
        }
    }
}
