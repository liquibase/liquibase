package liquibase.sql.visitor;

import liquibase.database.Database;

public class AppendSqlVisitor extends AbstractSqlVisitor{
    private String value;


    public String getName() {
        return "append";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String modifySql(String sql, Database database) {
        return sql + value;
    }
}