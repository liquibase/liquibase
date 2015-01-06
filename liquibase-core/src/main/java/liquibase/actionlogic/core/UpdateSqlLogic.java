package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.AbstractSqlAction;
import liquibase.action.Action;
import liquibase.action.UpdateAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.UpdateResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateSqlLogic extends AbstractSqlLogic {

    @Override
    public int getPriority(Action action, Scope scope) {
        if (action instanceof AbstractSqlAction && action instanceof UpdateAction) {
            return super.getPriority(action, scope);
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        try {
            AbstractJdbcDatabase database = scope.get(ScopeAttributes.database, AbstractJdbcDatabase.class);
            DatabaseConnection connection = database.getConnection();

            Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
            Statement stmt = jdbcConnection.createStatement();
            return new UpdateResult(stmt.executeUpdate(((AbstractSqlAction) action).getAttribute(AbstractSqlAction.Attr.sql, String.class)));

        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
