package liquibase.actionlogic.core.perform;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.Action;
import liquibase.action.ExecuteAction;
import liquibase.action.core.ExecuteSqlAction;
import liquibase.actionlogic.ActionLogicPriority;
import liquibase.actionlogic.ExecuteActionPerformLogic;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ExecuteSqlPerformLogic extends AbstractSqlPerformLogic implements ExecuteActionPerformLogic {

    @Override
    public ActionLogicPriority getPriority(Action action, Scope scope) {
        if (action instanceof ExecuteSqlAction) {
            return super.getPriority(action, scope);
        } else {
            return ActionLogicPriority.NOT_APPLICABLE;
        }
    }

    @Override
    public ExecuteAction.Result execute(ExecuteAction action, Scope scope) throws ActionPerformException {
        try {
            AbstractJdbcDatabase database = scope.get(ScopeAttributes.database, AbstractJdbcDatabase.class);
            DatabaseConnection connection = database.getConnection();

            Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
            Statement stmt = jdbcConnection.createStatement();
            stmt.execute(((ExecuteSqlAction) action).getSql());
            return new ExecuteAction.Result();

        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
