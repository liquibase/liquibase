package liquibase.actionlogic.core.perform;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.Action;
import liquibase.action.QueryAction;
import liquibase.action.core.QueryColumnsMetaDataAction;
import liquibase.actionlogic.ActionLogicPriority;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class QueryColumnsMetaDataPerformLogic extends AbstractJdbcMetaDataPerformLogic {

    @Override
    public ActionLogicPriority getPriority(Action action, Scope scope) {
        if (action instanceof QueryColumnsMetaDataAction) {
            return super.getPriority(action, scope);
        } else {
            return ActionLogicPriority.NOT_APPLICABLE;
        }
    }

    @Override
    public QueryAction.Result query(QueryAction action, Scope scope) throws ActionPerformException {
        AbstractJdbcDatabase database = scope.get(ScopeAttributes.database, AbstractJdbcDatabase.class);
        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

        QueryColumnsMetaDataAction queryAction = (QueryColumnsMetaDataAction) action;
        try {
            return new QueryAction.Result(JdbcUtils.extract(underlyingConnection.getMetaData().getColumns(queryAction.getCatalogName(), queryAction.getSchemaName(), queryAction.getTableName(), queryAction.getColumnName())));
        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
