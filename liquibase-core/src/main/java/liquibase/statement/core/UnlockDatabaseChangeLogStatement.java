package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class UnlockDatabaseChangeLogStatement extends AbstractSqlStatement {
    private final String lockedById;

    public UnlockDatabaseChangeLogStatement(String lockedById) {
        this.lockedById = lockedById;
    }

    public String getLockedById() {
        return lockedById;
    }
}

