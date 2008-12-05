package liquibase.database.sql.visitor;

public interface SqlStatementVisitor {

    String modifySql(String sql);
}
