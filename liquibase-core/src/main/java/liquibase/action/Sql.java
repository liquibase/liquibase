package liquibase.action;

public interface Sql extends UpdateAction, QueryAction, ExecuteAction {

    /**
     * Return the SQL command, not including the end delimiter. {@link liquibase.action.Action#describe()} should return the SQL with the delimiter.
     */
    String getSql();

    /**
     * Return the delimiter to use at the end of the statement.
     */
    String getEndDelimiter();

}
