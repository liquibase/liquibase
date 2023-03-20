package liquibase.logging.mdc.customobjects;

import liquibase.diff.DiffResult;
import liquibase.logging.mdc.CustomMdcObject;

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
        this.missing = diffResult.getMissingObjects().size();
        this.unexpected = diffResult.getUnexpectedObjects().size();
        this.changed = diffResult.getChangedObjects().entrySet().stream()
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
