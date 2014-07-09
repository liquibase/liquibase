package liquibase.executor;

/**
 * Container for results from an "Update" database command.
 */
public class UpdateResult {
    private long rowsUpdated;

    public UpdateResult(long rowsUpdated) {
        this.rowsUpdated = rowsUpdated;
    }

    /**
     * Returns the number of rows updated.
     */
    public long getRowsUpdated() {
        return rowsUpdated;
    }
}
