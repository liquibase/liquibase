package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class RemoveStaleLocksStatement extends AbstractSqlStatement {

    private final long maxTTLInSeconds;

    public RemoveStaleLocksStatement(long maxTTLInSeconds) {
        this.maxTTLInSeconds = maxTTLInSeconds;
    }

    public long getMaxTTLInSeconds() {
        return maxTTLInSeconds;
    }
}
