package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class TagDatabaseStatement implements SqlStatement {

    private String tag;

    public TagDatabaseStatement(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
