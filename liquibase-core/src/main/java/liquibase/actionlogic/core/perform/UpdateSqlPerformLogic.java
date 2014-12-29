package liquibase.actionlogic.core.perform;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.Action;
import liquibase.action.UpdateAction;
import liquibase.action.core.UpdateSqlAction;
import liquibase.actionlogic.ActionLogicPriority;
import liquibase.actionlogic.UpdateActionPerformLogic;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateSqlPerformLogic extends AbstractSqlPerformLogic implements UpdateActionPerformLogic {

    @Override
    public ActionLogicPriority getPriority(Action action, Scope scope) {
        if (action instanceof UpdateSqlAction) {
            return super.getPriority(action, scope);
        } else {
            return ActionLogicPriority.NOT_APPLICABLE;
        }
    }

    @Override
    public UpdateAction.Result update(UpdateAction action, Scope scope) throws ActionPerformException {
        try {
            AbstractJdbcDatabase database = scope.get(ScopeAttributes.database, AbstractJdbcDatabase.class);
            DatabaseConnection connection = database.getConnection();

            Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
            Statement stmt = jdbcConnection.createStatement();
            return new UpdateAction.Result(stmt.executeUpdate(((UpdateSqlAction) action).getSql()));

        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
