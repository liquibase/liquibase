package liquibase.database.sql;

import liquibase.database.Database;

public class RawSqlStatement implements SqlStatement {

    private String sql;


    public RawSqlStatement(String sql) {
        this.sql = sql;
    }

    public String getSqlStatement(Database database) {
        return sql;
    }
}
