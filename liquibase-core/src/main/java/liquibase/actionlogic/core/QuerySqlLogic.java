package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.QuerySqlAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class QuerySqlLogic extends AbstractSqlLogic<QuerySqlAction> {

    @Override
    protected Class<QuerySqlAction> getSupportedAction() {
        return QuerySqlAction.class;
    }

    @Override
    public ActionResult execute(QuerySqlAction action, Scope scope) throws ActionPerformException {
        try {
            AbstractJdbcDatabase database = (AbstractJdbcDatabase) scope.getDatabase();
            DatabaseConnection connection = database.getConnection();

            Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
            Statement stmt = jdbcConnection.createStatement();
            return new RowBasedQueryResult(JdbcUtils.extract(stmt.executeQuery(action.sql.toString())));
        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
