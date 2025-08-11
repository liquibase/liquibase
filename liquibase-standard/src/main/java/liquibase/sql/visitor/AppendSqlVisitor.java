package liquibase.sql.visitor;

import liquibase.database.Database;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppendSqlVisitor extends AbstractSqlVisitor{
    private String value;

    @Override
    public String getName() {
        return "append";
    }

    @Override
    public String modifySql(String sql, Database database) {
        return sql + value;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
