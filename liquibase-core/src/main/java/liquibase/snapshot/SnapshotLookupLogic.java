package liquibase.snapshot;

import liquibase.ExecutionEnvironment;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedException;
import liquibase.statement.Statement;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

public interface SnapshotLookupLogic {

    final int PRIORITY_NONE = 0;
    final int PRIORITY_DEFAULT = 10;
    final int PRIORITY_OBJECT = 50;
    final int PRIORITY_DATABASE = 100;

    int getPriority(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment);

    /**
     * Snapshot all objects of the given objectType that apply to the passed example.
     * For example, if objectType is {@link liquibase.structure.core.Column} and example is {@link liquibase.structure.core.Table}, then it should return all the columns for the table. If the example table has a null name, then all columns should be returned regardless of the table.
     */
    public <T extends DatabaseObject> Collection<T> lookup(Class<T> objectType, DatabaseObject example, ExecutionEnvironment environment) throws DatabaseException, UnsupportedException;

}
