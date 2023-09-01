package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForeignKeySnapshotGenerator extends JdbcSnapshotGenerator {

    protected static final String METADATA_DEFERRABILITY = "DEFERRABILITY";
    public static final String METADATA_FKTABLE_CAT = "FKTABLE_CAT";
    public static final String METADATA_FKTABLE_SCHEM = "FKTABLE_SCHEM";
    public static final String METADATA_FKTABLE_NAME = "FKTABLE_NAME";
    public static final String METADATA_FKCOLUMN_NAME = "FKCOLUMN_NAME";
    public static final String METADATA_PKTABLE_CAT = "PKTABLE_CAT";
    public static final String METADATA_PKTABLE_SCHEM = "PKTABLE_SCHEM";
    public static final String METADATA_PKTABLE_NAME = "PKTABLE_NAME";
    public static final String METADATA_PKCOLUMN_NAME = "PKCOLUMN_NAME";
    public static final String METADATA_UPDATE_RULE = "UPDATE_RULE";
    public static final String METADATA_DELETE_RULE = "DELETE_RULE";


    public ForeignKeySnapshotGenerator() {
        super(ForeignKey.class, new Class[]{Table.class});
    }


    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(ForeignKey.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema;
            schema = table.getSchema();


            Set<String> seenFks = new HashSet<>();
            List<CachedRow> importedKeyMetadataResultSet;
            try {
                importedKeyMetadataResultSet = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache().getForeignKeys(((AbstractJdbcDatabase) database)
                                .getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema),
                        database.correctObjectName(table.getName(), Table.class), null);

                for (CachedRow row : importedKeyMetadataResultSet) {
                    ForeignKey fk = new ForeignKey().setName(row.getString("FK_NAME")).setForeignKeyTable(table);
                    if (seenFks.add(fk.getName())) {
                        table.getOutgoingForeignKeys().add(fk);
                    }
                }
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {

        Database database = snapshot.getDatabase();

        List<CachedRow> importedKeyMetadataResultSet;
        try {
            Table fkTable = ((ForeignKey) example).getForeignKeyTable();
            String searchCatalog = ((AbstractJdbcDatabase) database).getJdbcCatalogName(fkTable.getSchema());
            String searchSchema = ((AbstractJdbcDatabase) database).getJdbcSchemaName(fkTable.getSchema());
            String searchTableName = database.correctObjectName(fkTable.getName(), Table.class);

            importedKeyMetadataResultSet = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache().getForeignKeys(searchCatalog,
                    searchSchema, searchTableName, example.getName());
            ForeignKey foreignKey = null;
            for (CachedRow row : importedKeyMetadataResultSet) {
                String fk_name = cleanNameFromDatabase(row.getString("FK_NAME"), database);
                if (snapshot.getDatabase().isCaseSensitive()) {
                    if (!fk_name.equals(example.getName())) {
                        continue;
                    } else if (!fk_name.equalsIgnoreCase(example.getName())) {
                        continue;
                    }
                }

                if (foreignKey == null) {
                    foreignKey = new ForeignKey();
                }

                foreignKey.setName(fk_name);

                String fkTableCatalog = cleanNameFromDatabase(row.getString(METADATA_FKTABLE_CAT), database);
                String fkTableSchema = cleanNameFromDatabase(row.getString(METADATA_FKTABLE_SCHEM), database);
                String fkTableName = cleanNameFromDatabase(row.getString(METADATA_FKTABLE_NAME), database);
                Table foreignKeyTable = new Table().setName(fkTableName);
                foreignKeyTable.setSchema(new Schema(new Catalog(fkTableCatalog), fkTableSchema));

                foreignKey.setForeignKeyTable(foreignKeyTable);
                Column fkColumn = new Column(cleanNameFromDatabase(row.getString(METADATA_FKCOLUMN_NAME), database)).setRelation(foreignKeyTable);
                boolean alreadyAdded = false;
                for (Column existing : foreignKey.getForeignKeyColumns()) {
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(existing, fkColumn, snapshot.getSchemaComparisons(), database)) {
                        alreadyAdded = true; //already added. One is probably an alias
                    }
                }
                if (alreadyAdded) {
                    break;
                }


                CatalogAndSchema pkTableSchema = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(
                        row.getString(METADATA_PKTABLE_CAT), row.getString(METADATA_PKTABLE_SCHEM));
                Table tempPkTable = (Table) new Table().setName(row.getString(METADATA_PKTABLE_NAME)).setSchema(
                        new Schema(pkTableSchema.getCatalogName(), pkTableSchema.getSchemaName()));
                foreignKey.setPrimaryKeyTable(tempPkTable);
                Column pkColumn = new Column(cleanNameFromDatabase(row.getString(METADATA_PKCOLUMN_NAME), database))
                        .setRelation(tempPkTable);

                foreignKey.addForeignKeyColumn(fkColumn);
                foreignKey.addPrimaryKeyColumn(pkColumn);
                //todo foreignKey.setKeySeq(importedKeyMetadataResultSet.getInt("KEY_SEQ"));

                // DB2 on z/OS doesn't support ON UPDATE
                if (!(database instanceof Db2zDatabase)) {
                    ForeignKeyConstraintType updateRule = convertToForeignKeyConstraintType(
                            row.getInt(METADATA_UPDATE_RULE), database);
                    foreignKey.setUpdateRule(updateRule);
                }
                ForeignKeyConstraintType deleteRule = convertToForeignKeyConstraintType(
                        row.getInt(METADATA_DELETE_RULE), database);
                foreignKey.setDeleteRule(deleteRule);

                short deferrability = row.getShort(METADATA_DEFERRABILITY);

                // SQL Anywhere supports initially deferrable but does not support initially immediate,
                // but reports not deferrable as initially immediate.
                if (database instanceof SybaseASADatabase && deferrability == DatabaseMetaData.importedKeyInitiallyImmediate) {
                    deferrability = DatabaseMetaData.importedKeyNotDeferrable;
                }

                // Hsqldb doesn't handle setting this property correctly, it sets it to 0.
                // it should be set to DatabaseMetaData.importedKeyNotDeferrable(7)
                if ((deferrability == 0) || (deferrability == DatabaseMetaData.importedKeyNotDeferrable)) {
                    foreignKey.setDeferrable(false);
                    foreignKey.setInitiallyDeferred(false);
                } else if (deferrability == DatabaseMetaData.importedKeyInitiallyDeferred) {
                    foreignKey.setDeferrable(true);
                    foreignKey.setInitiallyDeferred(true);
                } else if (deferrability == DatabaseMetaData.importedKeyInitiallyImmediate) {
                    foreignKey.setDeferrable(true);
                    foreignKey.setInitiallyDeferred(false);
                } else {
                    throw new RuntimeException("Unknown deferrability result: " + deferrability);
                }
                setValidateOptionIfAvailable(database, foreignKey, row);

                Index exampleIndex = new Index().setRelation(foreignKey.getForeignKeyTable());
                exampleIndex.getColumns().addAll(foreignKey.getForeignKeyColumns());
                exampleIndex.addAssociatedWith(Index.MARK_FOREIGN_KEY);
                foreignKey.setBackingIndex(exampleIndex);

            }
            if (snapshot.get(ForeignKey.class).contains(foreignKey)) {
                return null;
            }
            return foreignKey;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Method to map 'validate' option for FK. This thing works only for ORACLE
     *
     * @param database   - DB where FK will be created
     * @param foreignKey - FK object to persist validate option
     * @param cachedRow  - it's a cache-map to get metadata about FK
     */
    private void setValidateOptionIfAvailable(Database database, ForeignKey foreignKey, CachedRow cachedRow) {
        if (!(database instanceof OracleDatabase)) {
            return;
        }
        final String constraintValidate = cachedRow.getString("FK_VALIDATE");
        final String VALIDATE = "VALIDATED";
        if (constraintValidate != null && !constraintValidate.isEmpty()) {
            foreignKey.setShouldValidate(VALIDATE.equals(cleanNameFromDatabase(constraintValidate.trim(), database)));
        }
    }


    protected ForeignKeyConstraintType convertToForeignKeyConstraintType(Integer jdbcType, Database database) throws DatabaseException {
        if (jdbcType == null) {
            return ForeignKeyConstraintType.importedKeyRestrict;
        }
        if (driverUsesSpFkeys(database)) {
            if (jdbcType == 0) {
                return ForeignKeyConstraintType.importedKeyCascade;
            } else if (jdbcType == 1) {
                return ForeignKeyConstraintType.importedKeyNoAction;
            } else if (jdbcType == 2) {
                return ForeignKeyConstraintType.importedKeySetNull;
            } else if (jdbcType == 3) {
                return ForeignKeyConstraintType.importedKeySetDefault;
            } else {
                throw new DatabaseException("Unknown constraint type: " + jdbcType);
            }
        } else if (database instanceof SybaseDatabase) {
            /*If the database used is Sybase only omit the tags onUpdate and onDelete*/

            return null;
        } else {
            if (jdbcType == DatabaseMetaData.importedKeyCascade) {
                return ForeignKeyConstraintType.importedKeyCascade;
            } else if (jdbcType == DatabaseMetaData.importedKeyNoAction) {
                if (database instanceof SybaseASADatabase) {
                    //SQL Anywhere doesn't support NO ACTION, but reports it instead of SET DEFAULT
                    return ForeignKeyConstraintType.importedKeySetDefault;
                }
                return ForeignKeyConstraintType.importedKeyNoAction;
            } else if (jdbcType == DatabaseMetaData.importedKeyRestrict) {
                if (database instanceof MSSQLDatabase) {
                    //mssql doesn't support restrict. Not sure why it comes back with this type sometimes
                    return ForeignKeyConstraintType.importedKeyNoAction;
                } else {
                    return ForeignKeyConstraintType.importedKeyRestrict;
                }
            } else if (jdbcType == DatabaseMetaData.importedKeySetDefault) {
                return ForeignKeyConstraintType.importedKeySetDefault;
            } else if (jdbcType == DatabaseMetaData.importedKeySetNull) {
                return ForeignKeyConstraintType.importedKeySetNull;
            } else {
                throw new DatabaseException("Unknown constraint type: " + jdbcType);
            }
        }
    }

    /*
     * Sql server JDBC drivers prior to 6.3.3 used sp_fkeys to determine the delete/cascade metadata.
     * The sp_fkeys stored procedure spec says that returned integer values of 0, 1, 2, or 4
     * translate to cascade, noAction, SetNull, or SetDefault which are not the values in the JDBC
     * standard.
     *
     * If this method returns true, the sp_fkeys values should be used. Otherwise use the standard jdbc logic
     *
     * The change in logic went in with https://github.com/Microsoft/mssql-jdbc/pull/490
     */
    private boolean driverUsesSpFkeys(Database database) throws DatabaseException {
        if (!(database instanceof MSSQLDatabase)) {
            return false;
        }
        DatabaseConnection connection = database.getConnection();
        if (!(connection instanceof JdbcConnection)) {
            return false;
        }

        try {
            DatabaseMetaData metaData = ((JdbcConnection) connection).getMetaData();
            int driverMajorVersion = metaData.getDriverMajorVersion();
            int driverMinorVersion= metaData.getDriverMinorVersion();
            String driverName = metaData.getDriverName();

            if (!driverName.startsWith("Microsoft")) {
                return false;
            }

            return driverMajorVersion <= 6 && (driverMajorVersion != 6 || driverMinorVersion < 3);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

}
