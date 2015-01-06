package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.QueryTablesMetaDataAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class QueryTablesMetaDataLogic extends AbstractJdbcMetaDataLogic {

    @Override
    public int getPriority(Action action, Scope scope) {
        if (action instanceof QueryTablesMetaDataAction) {
            return super.getPriority(action, scope);
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        AbstractJdbcDatabase database = scope.get(Scope.Attr.database, AbstractJdbcDatabase.class);
        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

        QueryTablesMetaDataAction queryAction = (QueryTablesMetaDataAction) action;
        try {
            return new RowBasedQueryResult(JdbcUtils.extract(underlyingConnection.getMetaData().getTables(
                    queryAction.getAttribute(QueryTablesMetaDataAction.Attr.catalogName, String.class),
                    queryAction.getAttribute(QueryTablesMetaDataAction.Attr.schemaName, String.class),
                    queryAction.getAttribute(QueryTablesMetaDataAction.Attr.tableName, String.class),
                    new String[]{"TABLE"})));
        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
