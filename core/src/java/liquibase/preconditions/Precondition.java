package liquibase.preconditions;

import liquibase.migrator.Migrator;
import liquibase.exception.PreconditionFailedException;
import liquibase.DatabaseChangeLog;

/**
 * Marker interface for preconditions.  May become an annotation in the future.
 */
public interface Precondition {
    public void check(Migrator migrator, DatabaseChangeLog changeLog) throws PreconditionFailedException;

    public String getTagName();
}
