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

    public String getSqlStatement(Database database) {
        return sql;
    }


    public String getEndDelimiter(Database database) {
        return endDelimiter;
    }
}
