package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class MockSqlStatement implements SqlStatement {
    public boolean skipOnUnsupported() {
        return false;
    }
}
