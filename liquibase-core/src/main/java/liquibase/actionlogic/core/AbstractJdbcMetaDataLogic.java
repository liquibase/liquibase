package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.ObjectBasedQueryResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;
import liquibase.util.SmartMap;
import liquibase.util.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJdbcMetaDataLogic extends AbstractSnapshotLogic {

    @Override
    public int getPriority(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        if (database != null && database instanceof AbstractJdbcDatabase) {
            DatabaseConnection connection = database.getConnection();
            if (connection != null && connection instanceof JdbcConnection && ((JdbcConnection) connection).getUnderlyingConnection() != null) {
                return super.getPriority(action, scope);
            }
        }
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        AbstractJdbcDatabase database = scope.get(Scope.Attr.database, AbstractJdbcDatabase.class);
        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

        SnapshotDatabaseObjectsAction queryAction = (SnapshotDatabaseObjectsAction) action;
        DatabaseObject relatedTo = queryAction.getAttribute(SnapshotDatabaseObjectsAction.Attr.relatedTo, DatabaseObject.class);
        Class<? extends DatabaseObject> typeToSnapshot = action.getAttribute(SnapshotDatabaseObjectsAction.Attr.typeToSnapshot, Class.class);
        try {

            DatabaseMetaData metaData = underlyingConnection.getMetaData();

            List<SmartMap> rawRows = readRawMetaData(relatedTo, typeToSnapshot, metaData, scope);

            List<DatabaseObject> databaseObjects = new ArrayList<DatabaseObject>();
            for (SmartMap row : rawRows) {
                databaseObjects.add(convertToObject(row, typeToSnapshot, scope));
            }

            return new ObjectBasedQueryResult(databaseObjects);

        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }

    }

    protected abstract List<SmartMap> readRawMetaData(DatabaseObject relatedTo, Class<? extends DatabaseObject> typeToSnapshot, DatabaseMetaData metaData, Scope scope) throws SQLException;

    protected abstract DatabaseObject convertToObject(SmartMap row, Class outputType, Scope scope);

    protected String cleanNameFromDatabase(String name, Scope scope) {
        return StringUtils.trimToNull(name);
    }


}
