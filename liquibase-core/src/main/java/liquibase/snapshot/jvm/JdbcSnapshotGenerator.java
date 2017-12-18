package liquibase.snapshot.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.structure.DatabaseObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JdbcSnapshotGenerator implements SnapshotGenerator {
    private Set<DiffStatusListener> statusListeners = new HashSet<>();

    private Class<? extends DatabaseObject> defaultFor;
    private Class<? extends DatabaseObject>[] addsTo;

    protected JdbcSnapshotGenerator(Class<? extends DatabaseObject> defaultFor) {
        this.defaultFor = defaultFor;
    }

    protected JdbcSnapshotGenerator(Class<? extends DatabaseObject> defaultFor, Class<? extends DatabaseObject>[] addsTo) {
        this.defaultFor = defaultFor;
        this.addsTo = addsTo;
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof AbstractJdbcDatabase) {
            if ((defaultFor != null) && defaultFor.isAssignableFrom(objectType)) {
                return PRIORITY_DEFAULT;
            }
            if (addsTo() != null) {
                for (Class<? extends DatabaseObject> type : addsTo()) {
                    if (type.isAssignableFrom(objectType)) {
                        return PRIORITY_ADDITIONAL;
                    }
                }
            }
        }
        return PRIORITY_NONE;

    }

    @Override
    public Class<? extends DatabaseObject>[] addsTo() {
        return addsTo;
    }

    @Override
    public DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        if ((defaultFor != null) && defaultFor.isAssignableFrom(example.getClass())) {
            return snapshotObject(example, snapshot);
        }

        DatabaseObject chainResponse = chain.snapshot(example, snapshot);
        if (chainResponse == null) {
            return null;
        }

        if (shouldAddTo(example.getClass(), snapshot)) {
            if (addsTo() != null) {
                for (Class<? extends DatabaseObject> addType : addsTo()) {
                    if (addType.isAssignableFrom(example.getClass())) {
                        if (chainResponse != null) {
                            addTo(chainResponse, snapshot);
                        }
                    }
                }
            }
        }
        return chainResponse;

    }

    protected boolean shouldAddTo(Class<? extends DatabaseObject> databaseObjectType, DatabaseSnapshot snapshot) {
        return (defaultFor != null) && snapshot.getSnapshotControl().shouldInclude(defaultFor);
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return null;
    }

    protected abstract DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException;

    protected abstract void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException;

    public void addStatusListener(DiffStatusListener listener) {
        statusListeners.add(listener);
    }

    protected void updateListeners(String message) {
        if (this.statusListeners == null) {
            return;
        }
        LogService.getLog(getClass()).debug(LogType.LOG, message);
        for (DiffStatusListener listener : this.statusListeners) {
            listener.statusUpdate(message);
        }
    }

    protected String cleanNameFromDatabase(String objectName, Database database) {
        if (objectName == null) {
            return null;
        }
        if (!(database instanceof InformixDatabase)) {
            objectName = objectName.trim();
        }
        if (database instanceof PostgresDatabase) {
            return objectName.replaceAll("\"", "");
        }
        return objectName;
    }
    
    /**
     * Fetches an array of Strings with the catalog names in the database.
     * @param database The database from which to get the schema names
     * @return An array of catalog name Strings (May be an empty array)
     * @throws SQLException propagated java.sql.SQLException
     * @throws DatabaseException if a different problem occurs during the DBMS-specific code
     */
    protected String[] getDatabaseCatalogNames(Database database) throws SQLException, DatabaseException {
        List<String> returnList = new ArrayList<>();
        
        ResultSet catalogs = null;
        
        try {
            if (((AbstractJdbcDatabase) database).jdbcCallsCatalogsSchemas()) {
                catalogs = ((JdbcConnection) database.getConnection()).getMetaData().getSchemas();
            } else {
                catalogs = ((JdbcConnection) database.getConnection()).getMetaData().getCatalogs();
            }
            while (catalogs.next()) {
                if (((AbstractJdbcDatabase) database).jdbcCallsCatalogsSchemas()) {
                    returnList.add(catalogs.getString("TABLE_SCHEM"));
                } else {
                    returnList.add(catalogs.getString("TABLE_CAT"));
                }
            }
        } finally {
            if (catalogs != null) {
                try {
                    catalogs.close();
                } catch (SQLException ignore) {
                
                }
            }
            
        }
        return returnList.toArray(new String[returnList.size()]);
    }
    
}
