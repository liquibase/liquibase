package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class TagDatabaseStatement implements SqlStatement {

    private String tag;

    public TagDatabaseStatement(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
