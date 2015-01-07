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

public class QuerySqlLogic extends AbstractSqlLogic {

    @Override
    public int getPriority(Action action, Scope scope) {
        if (action instanceof QuerySqlAction) {
            return super.getPriority(action, scope);
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        try {
            AbstractJdbcDatabase database = scope.get(Scope.Attr.database, AbstractJdbcDatabase.class);
            DatabaseConnection connection = database.getConnection();

            Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
            Statement stmt = jdbcConnection.createStatement();
            return new RowBasedQueryResult(JdbcUtils.extract(stmt.executeQuery(action.getAttribute(QuerySqlAction.Attr.sql, String.class))));
        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
