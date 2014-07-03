package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class SelectMetaDataStatement extends AbstractStatement {

    private DatabaseObject example;

    public SelectMetaDataStatement(DatabaseObject example) {
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
