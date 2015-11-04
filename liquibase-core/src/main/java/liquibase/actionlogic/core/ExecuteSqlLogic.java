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
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ExecuteSqlLogic extends AbstractSqlLogic<ExecuteSqlAction>  {

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
    public ActionResult execute(ExecuteSqlAction action, Scope scope) throws ActionPerformException {
        AbstractJdbcDatabase database = (AbstractJdbcDatabase) scope.getDatabase();
        DatabaseConnection connection = database.getConnection();

        Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
        try {
            Statement stmt = jdbcConnection.createStatement();
            stmt.execute(action.sql.toString());
        } catch (SQLException e) {
            throw new ActionPerformException("Error executing SQL: "+action.sql.toString(), e);
        }
        return new ExecuteResult();

    }
}
