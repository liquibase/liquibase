package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotControl;
import liquibase.structure.core.*;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.structure.DatabaseObject;

public class JdbcDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {

    public boolean supports(Database database) {
        return true;
    }

    public int getPriority(Database database) {
        return PRIORITY_DEFAULT;
    }

    public Table getDatabaseChangeLogTable(Database database) throws DatabaseException {
        return getTable(database.correctSchema(new CatalogAndSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogTableName(), database);
    }

    public Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        return getTable(database.correctSchema(new CatalogAndSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogLockTableName(), database);
    }

    public boolean hasDatabaseChangeLogTable(Database database) throws DatabaseException {
        return hasTable(database.correctSchema(new CatalogAndSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogTableName(), database);
    }

    public boolean hasDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        return hasTable(database.correctSchema(new CatalogAndSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogLockTableName(), database);
    }

    private boolean hasTable(CatalogAndSchema schema, String tableName, Database database) throws DatabaseException {
        return DatabaseObjectGeneratorFactory.getInstance().getGenerator(Table.class, database).has((Table) new Table().setName(tableName).setSchema(new Schema(schema.getCatalogName(), schema.getSchemaName())), database);
    }

    private Table getTable(CatalogAndSchema schema, String tableName, Database database) throws DatabaseException {
        return DatabaseObjectGeneratorFactory.getInstance().getGenerator(Table.class, database).snapshot((Table) new Table().setName(tableName).setSchema(new Schema(schema.getCatalogName(), schema.getSchemaName())), database);
    }

    public DatabaseSnapshot createSnapshot(Database database, SnapshotControl snapshotControl) throws DatabaseException {
        DatabaseSnapshot snapshot = new DatabaseSnapshot(database, snapshotControl);

        for (CatalogAndSchema schema : snapshotControl.getSchemas()) {
            snapshot.addSchema(snapshot(new Schema(schema.getCatalogName(), schema.getSchemaName()), snapshot));
        }
        return snapshot;
    }

    public <DatabaseObjectType extends DatabaseObject> DatabaseObjectType snapshot(DatabaseObjectType example, DatabaseSnapshot snapshot) throws DatabaseException {
        Class<? extends DatabaseObject> objectType = example.getClass();
        DatabaseObjectSnapshotGenerator<DatabaseObjectType> generator = (DatabaseObjectSnapshotGenerator<DatabaseObjectType>) DatabaseObjectGeneratorFactory.getInstance().getGenerator(objectType, snapshot.getDatabase());
        return generator.snapshot(example, snapshot.getDatabase());
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
}
