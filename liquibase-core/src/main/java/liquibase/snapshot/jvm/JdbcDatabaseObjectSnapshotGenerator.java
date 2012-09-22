package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JdbcDatabaseObjectSnapshotGenerator<DatabaseObjectType extends DatabaseObject> implements DatabaseObjectSnapshotGenerator<DatabaseObjectType> {
    private Set<DiffStatusListener> statusListeners = new HashSet<DiffStatusListener>();

    public void addStatusListener(DiffStatusListener listener) {
        statusListeners.add(listener);
    }

    public boolean supports(Class<? extends DatabaseObject> databaseObjectClass, Database database) {
        return true;
    }

    protected DatabaseMetaData getMetaData(Database database) throws SQLException {
        DatabaseMetaData databaseMetaData = null;
        if (database.getConnection() != null) {
            databaseMetaData = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().getMetaData();
        }
        return databaseMetaData;
    }

    protected void updateListeners(String message) {
        if (this.statusListeners == null) {
            return;
        }
        LogFactory.getLogger().debug(message);
        for (DiffStatusListener listener : this.statusListeners) {
            listener.statusUpdate(message);
        }
    }

    protected String cleanNameFromDatabase(String objectName, Database database) {
        if (objectName == null) {
            return null;
        }
        if (database instanceof PostgresDatabase) {
            return objectName.replaceAll("\"", "");
        }
        return objectName;
    }

    protected Set<String> listAllTables(Schema schema, Database database) throws DatabaseException {
        Set<String> returnTables = new HashSet<String>();
        ResultSet tableMetaDataRs = null;
        try {
            tableMetaDataRs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), null, new String[]{"TABLE"});
            while (tableMetaDataRs.next()) {
                returnTables.add(tableMetaDataRs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            try {
                if (tableMetaDataRs != null) {
                    tableMetaDataRs.close();
                }
            } catch (SQLException ignore) {
            }
        }
        return returnTables;

    }
}
