package liquibase.statement.core;

import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

public class MockSqlStatement implements SqlStatement {
    @Override
    public boolean skipOnUnsupported() {
        return false;
    }

    @Override
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
