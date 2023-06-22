package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class TagDatabaseStatement extends AbstractSqlStatement {

    private final String tag;

    public TagDatabaseStatement(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
