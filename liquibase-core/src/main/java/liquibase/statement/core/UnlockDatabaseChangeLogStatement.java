package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Unlock the Liquibase database changelog lock.
 */
public class UnlockDatabaseChangeLogStatement extends AbstractStatement {
    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}

