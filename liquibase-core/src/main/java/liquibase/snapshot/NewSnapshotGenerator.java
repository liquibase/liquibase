package liquibase.snapshot;

import liquibase.ExecutionEnvironment;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedException;
import liquibase.statement.Statement;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

public interface NewSnapshotGenerator {

    final int PRIORITY_NONE = 0;
    final int PRIORITY_DEFAULT = 10;
    final int PRIORITY_OBJECT = 50;
    final int PRIORITY_DATABASE = 100;

    int getPriority(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment);

    public <T extends DatabaseObject> Collection<T> lookupFor(DatabaseObject example, Class<T> objectType, ExecutionEnvironment environment) throws DatabaseException, UnsupportedException;

    void relate(Class<? extends DatabaseObject> objectType, NewDatabaseSnapshot snapshot);

}
