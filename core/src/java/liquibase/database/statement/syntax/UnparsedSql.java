package liquibase.database.statement.syntax;

public class UnparsedSql implements Sql {

    private String sql;

    public UnparsedSql(String sql) {
        this.sql = sql;
    }

    public String toSql() {
        return sql;
    }
}
