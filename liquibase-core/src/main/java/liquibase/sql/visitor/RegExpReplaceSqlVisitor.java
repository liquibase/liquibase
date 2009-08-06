package liquibase.sql.visitor;

import liquibase.database.Database;

public class RegExpReplaceSqlVisitor extends AbstractSqlVisitor {

    private String replace;
    private String with;

    public String getTagName() {
        return "regExpReplace";
    }

    public String getReplace() {
        return replace;
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }

    public String getWith() {
        return with;
    }

    public void setWith(String with) {
        this.with = with;
    }

    public String modifySql(String sql, Database database) {
        return sql.replaceAll(getReplace(), getWith());
    }
}
