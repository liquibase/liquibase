package liquibase.sql.visitor;

import liquibase.database.Database;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReplaceSqlVisitor extends AbstractSqlVisitor {

    private String replace;
    private String with;

    @Override
    public String getName() {
        return "replace";
    }

    @Override
    public String modifySql(String sql, Database database) {
        return sql.replace(getReplace(), getWith());
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
