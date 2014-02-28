package liquibase.sdk.supplier.change;

import liquibase.change.Change;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;

public interface ChangeSupplier<T extends Change> {
    Change[] prepareDatabase(T change) throws Exception;

    void checkDiffResult(DiffResult diffResult, T change) throws Exception;

    Change[] revertDatabase(T change) throws Exception;
}
