package liquibase.statement.core;

import liquibase.statement.CallableStatement;

public class RawCallStatement extends RawSqlStatement implements CallableStatement {

    public RawCallStatement(String sql) {
        super(sql);
    }

    public RawCallStatement(String sql, String endDelimiter) {
        super(sql, endDelimiter);
    }
}
