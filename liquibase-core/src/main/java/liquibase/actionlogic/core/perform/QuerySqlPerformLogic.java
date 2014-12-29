package liquibase.actionlogic.core.perform;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.Action;
import liquibase.action.QueryAction;
import liquibase.action.core.QuerySqlAction;
import liquibase.actionlogic.ActionLogicPriority;
import liquibase.actionlogic.QueryActionPerformLogic;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.util.JdbcUtils;

import java.sql.*;

public class QuerySqlPerformLogic extends AbstractSqlPerformLogic implements QueryActionPerformLogic {

    @Override
    public ActionLogicPriority getPriority(Action action, Scope scope) {
        if (action instanceof QuerySqlAction) {
            return super.getPriority(action, scope);
        } else {
            return ActionLogicPriority.NOT_APPLICABLE;
        }
    }

    @Override
    public QueryAction.Result query(QueryAction action, Scope scope) throws ActionPerformException {
        try {
            AbstractJdbcDatabase database = scope.get(ScopeAttributes.database, AbstractJdbcDatabase.class);
            DatabaseConnection connection = database.getConnection();

            Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
            Statement stmt = jdbcConnection.createStatement();
            return new QueryAction.Result(JdbcUtils.extract(stmt.executeQuery(((QuerySqlAction) action).getSql())));
        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
