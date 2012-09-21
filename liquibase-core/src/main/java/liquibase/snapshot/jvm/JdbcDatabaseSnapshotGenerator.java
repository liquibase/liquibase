package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.core.*;
import liquibase.diff.DiffControl;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.structure.DatabaseObject;

import java.util.*;

public class JdbcDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {

    private Set<DiffStatusListener> statusListeners;

    public boolean supports(Database database) {
        return true;
    }

    public int getPriority(Database database) {
        return PRIORITY_DEFAULT;
    }

    public Table getDatabaseChangeLogTable(Database database) throws DatabaseException {
        return getTable(database.correctSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogTableName(), database);
    }

    public Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        return getTable(database.correctSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogLockTableName(), database);
    }

    public boolean hasDatabaseChangeLogTable(Database database) throws DatabaseException {
        return hasTable(database.correctSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogTableName(), database);
    }

    public boolean hasDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        return hasTable(database.correctSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogLockTableName(), database);
    }

    private boolean hasTable(Schema schema, String tableName, Database database) throws DatabaseException {
        return DatabaseObjectGeneratorFactory.getInstance().getGenerator(Table.class, database).has(schema, new Table().setName(tableName), database);
    }

    private Table getTable(Schema schema, String tableName, Database database) throws DatabaseException {
        return DatabaseObjectGeneratorFactory.getInstance().getGenerator(Table.class, database).get(schema, new Table().setName(tableName), database);
    }

    public DatabaseSnapshot createSnapshot(Database database, DiffControl diffControl, DiffControl.DatabaseRole type) throws DatabaseException {
        DatabaseSnapshot snapshot = new DatabaseSnapshot(database, diffControl.getSchemas(type));
        this.statusListeners = diffControl.getStatusListeners();

        for (Schema schema : diffControl.getSchemas(type)) {
            schema = snapshot.getDatabase().correctSchema(schema);
            for (Class clazz :  ServiceLocator.getInstance().findClasses(DatabaseObject.class)) {
                DatabaseObjectSnapshotGenerator generator = DatabaseObjectGeneratorFactory.getInstance().getGenerator(clazz, database);
                if (generator != null) {
                    updateListeners("Reading objects of type "+clazz.getName()+" from "+schema);
                    DatabaseObject[] objects = generator.get(schema, database);
                    if (objects != null) {
                        snapshot.addDatabaseObjects(objects);
                    }
                }
            }
        }

        snapshot.merge();

        return snapshot;

    }



//    public boolean hasForeignKey(Schema schema, String foreignKeyTableName, String fkName, Database database) throws DatabaseException {
//        ForeignKey fk = new ForeignKey().setName(fkName);
//        try {
//            ResultSet rs = getMetaData(database).getImportedKeys(getJdbcCatalogName(schema), getJdbcSchemaName(schema), database.correctObjectName(foreignKeyTableName, ForeignKey.class));
//            try {
//                while (rs.next()) {
//                    ForeignKey foundFk = new ForeignKey().setName(rs.getString("FK_NAME"));
//                    if (fk.equals(foundFk, database)) {
//                        return true;
//                    }
//                }
//                return false;
//            } finally {
//                try {
//                    rs.close();
//                } catch (SQLException ignore) {
//                }
//            }
//        } catch (Exception e) {
//            throw new UnexpectedLiquibaseException(e);
//        }
//    }

//    /**
//     * It finds <u>only</u> all database-specific Foreign Keys.
//     * By default it returns an empty ArrayList.
//     *
//     * @param schemaName current shemaName
//     * @param database   current database
//     * @return list of database-specific Foreing Keys
//     * @throws liquibase.exception.DatabaseException
//     *          any kinds of SQLException errors
//     */
//    protected List<ForeignKey> getAdditionalForeignKeys(String schemaName, Database database) throws DatabaseException {
//        return new ArrayList<ForeignKey>();
//    }


    protected boolean columnNamesAreEqual(String columnNames, String otherColumnNames, Database database) {
        if (database.isCaseSensitive()) {
            return columnNames.replace(" ", "").equals(otherColumnNames.replace(" ", ""));
        } else {
            return columnNames.replace(" ", "").equalsIgnoreCase(otherColumnNames.replace(" ", ""));
        }
    }

    protected boolean includeInSnapshot(DatabaseObject obj) {
        return true;
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
}
