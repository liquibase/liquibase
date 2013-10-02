package liquibase.precondition;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.serializer.LiquibaseSerializable;

/**
 * Marker interface for preconditions.  May become an annotation in the future.
 */
public interface Precondition {
    public String getName();

    public Warnings warn(Database database);

    public ValidationErrors validate(Database database);

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException;

}
