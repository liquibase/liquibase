package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;

public class TagDatabaseStatement extends AbstractSqlStatement {

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
