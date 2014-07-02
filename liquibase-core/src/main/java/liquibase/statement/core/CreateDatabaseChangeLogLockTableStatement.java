package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Creates the table to manage the database changelog lock
 */
public class CreateDatabaseChangeLogLockTableStatement extends AbstractStatement {

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
