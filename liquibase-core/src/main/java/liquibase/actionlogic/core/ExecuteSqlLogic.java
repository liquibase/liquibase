package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.actionlogic.ActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.ExecuteResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ExecuteSqlLogic extends AbstractSqlLogic<ExecuteSqlAction> implements ActionLogic.InteractsExternally<ExecuteSqlAction> {

    @Override
    protected Class<ExecuteSqlAction> getSupportedAction() {
        return ExecuteSqlAction.class;
    }

    @Override
    public int getPriority(ExecuteSqlAction action, Scope scope) {
        if (action instanceof ExecuteSqlAction) {
            Database database = scope.getDatabase();
            if (database == null || (!(database instanceof AbstractJdbcDatabase))) {
                return PRIORITY_NOT_APPLICABLE;
            }

            return super.getPriority(action, scope);
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public boolean interactsExternally(ExecuteSqlAction action, Scope scope) {
        return true;
    }

    @Override
    public ActionResult execute(ExecuteSqlAction action, Scope scope) throws ActionPerformException {
        try {
            AbstractJdbcDatabase database = (AbstractJdbcDatabase) scope.getDatabase();
            DatabaseConnection connection = database.getConnection();

            Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
            Statement stmt = jdbcConnection.createStatement();
            stmt.execute(action.sql.toString());
            return new ExecuteResult();

        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
