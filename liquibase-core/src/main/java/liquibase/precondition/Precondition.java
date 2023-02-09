package liquibase.precondition;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;

/**
 * Marker interface for preconditions.  May become an annotation in the future.
 */
public interface Precondition extends LiquibaseSerializable {
    String getName();

    Warnings warn(Database database);

    ValidationErrors validate(Database database);

    void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException;

    void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException;
}
