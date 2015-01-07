package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.ExecuteResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ExecuteSqlLogic extends AbstractSqlLogic {

    @Override
    public int getPriority(Action action, Scope scope) {
        if (action instanceof ExecuteSqlAction) {
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
            stmt.execute(action.getAttribute(ExecuteSqlAction.Attr.sql, String.class));
            return new ExecuteResult();

        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
