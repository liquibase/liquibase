package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class TagDatabaseStatement extends AbstractSqlStatement {

    private final String tag;

    public TagDatabaseStatement(String tag) {
        this.tag = tag;
    }

}
