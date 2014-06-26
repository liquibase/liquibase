package liquibase.statement.core;

import liquibase.statement.Statement;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

public class MockStatement implements Statement {

    private String id;

    public MockStatement() {
    }

    public MockStatement(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean skipOnUnsupported() {
        return false;
    }

    @Override
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
