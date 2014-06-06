package liquibase.executor;

public class UpdateResult {
    private long rowsUpdated;

    public UpdateResult(long rowsUpdated) {
        this.rowsUpdated = rowsUpdated;
    }

    public long getRowsUpdated() {
        return rowsUpdated;
    }
}
