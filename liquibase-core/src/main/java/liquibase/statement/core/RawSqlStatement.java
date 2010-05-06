package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class RawSqlStatement implements SqlStatement {

    private String sql;
    private String endDelimiter  = ";";


    public RawSqlStatement(String sql) {
        this.sql = sql;
    }

    public RawSqlStatement(String sql, String endDelimiter) {
        this(sql);
        if (endDelimiter != null) {
            this.endDelimiter = endDelimiter;
        }
    }

    public String getSql() {
        return sql;
    }

    public String getEndDelimiter() {
        return endDelimiter.replace("\\r","\r").replace("\\n","\n");
    }

    @Override
    public String toString() {
        return sql;
    }
}
