package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

public class FetchObjectsStatement extends AbstractSqlStatement {

    private DatabaseObject example;

    public FetchObjectsStatement(DatabaseObject example) {
        this.example = example;
    }

    public DatabaseObject getExample() {
        return example;
    }

    @Override
    public String toString() {
        return "Fetch "+example.getClass().getSimpleName()+"(s) like '"+example.toString()+"'";
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
