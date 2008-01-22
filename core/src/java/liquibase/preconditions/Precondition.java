package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionFailedException;

/**
 * Marker interface for preconditions.  May become an annotation in the future.
 */
public interface Precondition {
    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException;

    public String getTagName();
}
