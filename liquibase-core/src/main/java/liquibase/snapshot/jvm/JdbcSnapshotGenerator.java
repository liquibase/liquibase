package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.structure.DatabaseObject;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public abstract class JdbcSnapshotGenerator implements SnapshotGenerator {
    private Set<DiffStatusListener> statusListeners = new HashSet<DiffStatusListener>();

    private Class<? extends DatabaseObject> defaultFor = null;
    private Class<? extends DatabaseObject>[] addsTo = null;

    protected JdbcSnapshotGenerator(Class<? extends DatabaseObject> defaultFor) {
        this.defaultFor = defaultFor;
    }

    protected JdbcSnapshotGenerator(Class<? extends DatabaseObject> defaultFor, Class<? extends DatabaseObject>... addsTo) {
        this.defaultFor = defaultFor;
        this.addsTo = addsTo;
    }

    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (defaultFor != null && defaultFor.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        if (addsTo != null) {
            for (Class<? extends DatabaseObject> type : addsTo) {
                if (type.isAssignableFrom(objectType)) {
                    return PRIORITY_ADDITIONAL;
                }
            }
        }
        return PRIORITY_NONE;

    }


    public DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        if (defaultFor != null && defaultFor.isAssignableFrom(example.getClass())) {
            return snapshotObject(example, snapshot);
        }

        DatabaseObject chainResponse = chain.snapshot(example, snapshot);
        if (chainResponse == null) {
            return null;
        }
        if (addsTo != null) {
            for (Class<? extends DatabaseObject> addType : addsTo) {
                if (addType.isAssignableFrom(example.getClass())) {
                    if (chainResponse != null) {
                        addTo(chainResponse, snapshot);
                    }
                }
            }
        }
        return chainResponse;

    }

    protected abstract DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException;

    protected abstract void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException;

//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

    public void addStatusListener(DiffStatusListener listener) {
        statusListeners.add(listener);
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

    protected Set<String> listAllTables(CatalogAndSchema schema, Database database) throws DatabaseException {
        Set<String> returnTables = new HashSet<String>();
        ResultSet tableMetaDataRs = null;
        try {
            tableMetaDataRs = getMetaData(database).getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), null, new String[]{"TABLE"});
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
