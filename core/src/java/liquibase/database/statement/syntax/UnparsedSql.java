package liquibase.database.statement.syntax;

public class UnparsedSql implements Sql {

    private String sql;
    private String endDelimiter;

    public UnparsedSql(String sql) {
        this(sql, ";");
    }

    public UnparsedSql(String sql, String endDelimiter) {
        this.sql = sql.trim();
        this.endDelimiter = endDelimiter;
    }

    public String toSql() {
        return sql;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

}
