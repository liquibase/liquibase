package liquibase.database.sql.visitor;

public class RegExpReplaceSqlStatementVisitor implements SqlStatementVisitor {

    private String pattern;
    private String with;

    public RegExpReplaceSqlStatementVisitor(String pattern, String with) {
        this.pattern = pattern;
        this.with = with;
    }

    public String getPattern() {
        return pattern;
    }

    public String getWith() {
        return with;
    }

    public String modifySql(String sql) {
        return sql.replaceAll(getPattern(), getWith());
    }
}
