package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.AbstractSqlAction;
import liquibase.action.Action;
import liquibase.action.UpdateAction;
import liquibase.action.UpdateSqlAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.UpdateResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateSqlLogic extends AbstractSqlLogic<UpdateSqlAction> {

    @Override
    protected Class<UpdateSqlAction> getSupportedAction() {
        return UpdateSqlAction.class;
    }

    @Override
    public ActionResult execute(UpdateSqlAction action, Scope scope) throws ActionPerformException {
        try {
            AbstractJdbcDatabase database = (AbstractJdbcDatabase) scope.getDatabase();
            DatabaseConnection connection = database.getConnection();

            Connection jdbcConnection = ((JdbcConnection) connection).getUnderlyingConnection();
            Statement stmt = jdbcConnection.createStatement();
            return new UpdateResult(stmt.executeUpdate(action.sql.toString()));

        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
