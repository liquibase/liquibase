package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class RawSqlStatement extends AbstractStatement {

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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
