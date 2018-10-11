package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class LockDatabaseChangeLogStatement extends AbstractSqlStatement {

    /** Whether this lock will be actively prolonged or not */
    private final boolean prolongedLock;

    public LockDatabaseChangeLogStatement(boolean prolongedLock) {
        this.prolongedLock = prolongedLock;
    }

    public boolean isProlongedLock() {
        return prolongedLock;
    }
}
