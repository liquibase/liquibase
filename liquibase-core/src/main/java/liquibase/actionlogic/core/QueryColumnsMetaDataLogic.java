package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.Action;
import liquibase.action.core.QueryColumnsMetaDataAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class QueryColumnsMetaDataLogic extends AbstractJdbcMetaDataLogic {

    @Override
    public int getPriority(Action action, Scope scope) {
        if (action instanceof QueryColumnsMetaDataAction) {
            return super.getPriority(action, scope);
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        AbstractJdbcDatabase database = scope.get(ScopeAttributes.database, AbstractJdbcDatabase.class);
        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

        QueryColumnsMetaDataAction queryAction = (QueryColumnsMetaDataAction) action;
        try {
            return new RowBasedQueryResult(JdbcUtils.extract(underlyingConnection.getMetaData().getColumns(
                    queryAction.getAttribute(QueryColumnsMetaDataAction.Attr.catalogName, String.class),
                    queryAction.getAttribute(QueryColumnsMetaDataAction.Attr.schemaName, String.class),
                    queryAction.getAttribute(QueryColumnsMetaDataAction.Attr.tableName, String.class),
                    queryAction.getAttribute(QueryColumnsMetaDataAction.Attr.columnName, String.class))));
        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
