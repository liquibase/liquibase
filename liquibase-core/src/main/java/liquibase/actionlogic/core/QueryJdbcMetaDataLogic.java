package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.QueryJdbcMetaDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.util.JdbcUtils;
import liquibase.util.Validate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * Executes a method on {@link java.sql.DatabaseMetaData}.
 * No pre-processing of the arguments is performed. No post-processing of the results is performed.
 */
public class QueryJdbcMetaDataLogic extends AbstractActionLogic implements ActionLogic.InteractsExternally {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return QueryJdbcMetaDataAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(QueryJdbcMetaDataAction.Attr.method, action);
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return super.supportsScope(scope) && database instanceof AbstractJdbcDatabase;
    }

    @Override
    public boolean interactsExternally(Action action, Scope scope) {
        return true;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        String method = action.get(QueryJdbcMetaDataAction.Attr.method, String.class);
        List arguments = action.get(QueryJdbcMetaDataAction.Attr.arguments, List.class);
        try {
            if (method.equals("getTables")) {
                Validate.isTrue(arguments.size() == 4, "getTables requires 4 arguments");
                return new RowBasedQueryResult(JdbcUtils.extract(getMetaData(scope).getTables((String) arguments.get(0), (String) arguments.get(1), (String) arguments.get(2), (String[]) arguments.get(3))));
            } else if (method.equals("getColumns")) {
                Validate.isTrue(arguments.size() == 4, "getColumns requires 4 arguments");
                return new RowBasedQueryResult(JdbcUtils.extract(getMetaData(scope).getColumns((String) arguments.get(0), (String) arguments.get(1), (String) arguments.get(2), (String) arguments.get(3))));
            }
            throw new ActionPerformException("Unknown method '" + method + "'");
        } catch (Exception e) {
            throw new ActionPerformException(e);
        }
    }

    protected DatabaseMetaData getMetaData(Scope scope) throws DatabaseException {
        AbstractJdbcDatabase database = scope.get(Scope.Attr.database, AbstractJdbcDatabase.class);
        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

        try {
            return underlyingConnection.getMetaData();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }


}
