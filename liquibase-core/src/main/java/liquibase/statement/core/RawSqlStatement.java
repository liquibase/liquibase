package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class RawSqlStatement implements SqlStatement {

    private String sql;
    private String endDelimiter;


    public RawSqlStatement(String sql) {
        this.sql = sql;
    }

    public RawSqlStatement(String sql, String endDelimiter) {
        this(sql);
        this.endDelimiter = endDelimiter;
    }

    public String getSql() {
        return sql;
    }

    public String getEndDelimiter() {
        if (endDelimiter == null) {
            return null;
        }
        return endDelimiter.replace("\\r","\r").replace("\\n","\n");
    }

    @Override
    public String toString() {
        return sql;
    }
}
