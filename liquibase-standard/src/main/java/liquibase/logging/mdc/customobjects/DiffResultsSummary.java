package liquibase.logging.mdc.customobjects;

import liquibase.diff.DiffResult;
import liquibase.logging.mdc.CustomMdcObject;
import liquibase.structure.DatabaseObject;

public class DiffResultsSummary implements CustomMdcObject {
    private int missing;
    private int unexpected;
    private int changed;

    /**
     * Constructor for service locator.
     */
    public DiffResultsSummary() {
    }

    public DiffResultsSummary(DiffResult diffResult) {
        this.missing = 0;
        for (DatabaseObject missingObject : diffResult.getMissingObjects()) {
            if (diffResult.getReferenceSnapshot().getSnapshotControl().shouldInclude(missingObject)) {
                this.missing++;
            }
        }

        this.unexpected = 0;
        for (DatabaseObject unexpectedObject : diffResult.getUnexpectedObjects()) {
            if (diffResult.getReferenceSnapshot().getSnapshotControl().shouldInclude(unexpectedObject)) {
                this.unexpected++;
            }
        }

        this.changed = diffResult.getChangedObjects().entrySet().stream()
                .filter(entry -> diffResult.getReferenceSnapshot().getSnapshotControl().shouldInclude(entry.getKey()))
                .reduce(0, (subtotal, element) -> subtotal + element.getValue().getDifferences().size(), Integer::sum);
    }

    public int getMissing() {
        return missing;
    }

    public void setMissing(int missing) {
        this.missing = missing;
    }

    public int getUnexpected() {
        return unexpected;
    }

    public void setUnexpected(int unexpected) {
        this.unexpected = unexpected;
    }

    public int getChanged() {
        return changed;
    }

    public void setChanged(int changed) {
        this.changed = changed;
    }
}
