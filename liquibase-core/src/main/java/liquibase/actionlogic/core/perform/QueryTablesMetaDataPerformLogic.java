package liquibase.actionlogic.core.perform;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.Action;
import liquibase.action.QueryAction;
import liquibase.action.core.QueryTablesMetaDataAction;
import liquibase.actionlogic.ActionLogicPriority;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class QueryTablesMetaDataPerformLogic extends AbstractJdbcMetaDataPerformLogic {

    @Override
    public ActionLogicPriority getPriority(Action action, Scope scope) {
        if (action instanceof QueryTablesMetaDataAction) {
            return super.getPriority(action, scope);
        } else {
            return ActionLogicPriority.NOT_APPLICABLE;
        }
    }

    @Override
    public QueryAction.Result query(QueryAction action, Scope scope) throws ActionPerformException {
        AbstractJdbcDatabase database = scope.get(ScopeAttributes.database, AbstractJdbcDatabase.class);
        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

        QueryTablesMetaDataAction queryAction = (QueryTablesMetaDataAction) action;
        try {
            return new QueryAction.Result(JdbcUtils.extract(underlyingConnection.getMetaData().getTables(queryAction.getCatalogName(), queryAction.getSchemaName(), queryAction.getTableName(), new String[] {"TABLE"})));
        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
