package liquibase.sql.visitor;

import liquibase.database.Database;
import lombok.Getter;

@Getter
public class RegExpReplaceSqlVisitor extends AbstractSqlVisitor {

    private String replace;
    private String with;

    @Override
    public String getName() {
        return "regExpReplace";
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }

    public void setWith(String with) {
        this.with = with;
    }

    @Override
    public String modifySql(String sql, Database database) {
        return sql.replaceAll(getReplace(), getWith());
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
