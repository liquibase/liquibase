package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class RawSqlStatement extends AbstractSqlStatement {

    private String sql;
    private String endDelimiter  = ";";
    private boolean outputDelimiter;


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

    public boolean isOutputDelimiter() {
        return outputDelimiter;
    }

    public void setOutputDelimiter(boolean outputDelimiter) {
        this.outputDelimiter = outputDelimiter;
    }

    @Override
    public String toString() {
        return sql;
    }
}
