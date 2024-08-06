package liquibase.sql.visitor;

import liquibase.database.Database;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PrependSqlVisitor extends AbstractSqlVisitor {
    private String value;


    @Override
    public String getName() {
        return "prepend";
    }

    @Override
    public String modifySql(String sql, Database database) {
        return value + sql;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
