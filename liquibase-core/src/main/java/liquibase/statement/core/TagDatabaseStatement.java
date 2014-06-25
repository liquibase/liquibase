package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class TagDatabaseStatement extends AbstractStatement {

    private String tag;

    public TagDatabaseStatement(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
