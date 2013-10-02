package liquibase.sql.visitor;

import liquibase.database.Database;

public class PrependSqlVisitor extends AbstractSqlVisitor {
    private String value;


    public String getName() {
        return "prepend";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String modifySql(String sql, Database database) {
        return value + sql;
    }
}
