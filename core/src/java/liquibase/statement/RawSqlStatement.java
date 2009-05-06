package liquibase.statement;

public class RawSqlStatement implements SqlStatement {

    private String sql;
    private String endDelimiter  = ";";


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
        return endDelimiter;
    }

    public String toString() {
        return sql;
    }
}
