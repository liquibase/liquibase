package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.migrator.Migrator;

/**
 * Marker interface for preconditions.  May become an annotation in the future.
 */
public interface Precondition {
    public void check(Migrator migrator, DatabaseChangeLog changeLog) throws PreconditionFailedException;

    public String getTagName();
}
