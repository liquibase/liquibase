package liquibase.action;

import liquibase.Scope;
import liquibase.exception.DatabaseException;

/**
 * An interface for {@link liquibase.action.Action}s that updates data. This action can update data in a database, or any other location.
 * Implementations should only perform outside interaction from within the {@link #update( liquibase.Scope)} method.
 */
public interface UpdateAction extends Action {

    /**
     * Container for results from an "Update" database command.
     */
    public static class Result {
        private long rowsUpdated;

        public Result(long rowsUpdated) {
            this.rowsUpdated = rowsUpdated;
        }

        /**
         * Returns the number of rows updated.
         */
        public long getRowsUpdated() {
            return rowsUpdated;
        }
    }
}
