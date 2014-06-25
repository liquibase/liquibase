package liquibase.statement.core;

import liquibase.statement.Statement;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

public class MockStatement implements Statement {
    @Override
    public boolean skipOnUnsupported() {
        return false;
    }

    @Override
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
