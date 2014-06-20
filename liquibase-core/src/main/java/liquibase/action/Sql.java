package liquibase.action;

public interface Sql extends UpdateAction, QueryAction, ExecuteAction {
    public String toSql();

    String getEndDelimiter();

}
