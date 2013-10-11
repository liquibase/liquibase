package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class MockSqlStatement implements SqlStatement {
    @Override
    public boolean skipOnUnsupported() {
        return false;
    }
}
