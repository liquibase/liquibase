package liquibase.database.sql;

import liquibase.database.Database;

public class RawSqlStatement implements SqlStatement {

    private String sql;
    private String endDelimiter  = ";";;


    public RawSqlStatement(String sql) {
        this.sql = sql;
    }

    public RawSqlStatement(String sql, String endDelimiter) {
        this(sql);
        this.endDelimiter = endDelimiter;
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }

    public String getSqlStatement(Database database) {
        return sql;
    }


    public String getEndDelimiter(Database database) {
        return endDelimiter;
    }

    public String toString() {
        return sql;
    }
}
