package liquibase.sdk.supplier.change;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;

import java.util.Collection;
import java.util.List;

public interface ChangeSupplier<T extends Change> {
    Change[] prepareDatabase(T change) throws Exception;

    void checkDiffResult(DiffResult diffResult, T change) throws Exception;

    Change[] revertDatabase(T change) throws Exception;

    Collection<Change> getAllParameterPermutations(Database database) throws Exception;

    boolean isValid(Change change, Database database);
}
