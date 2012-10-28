package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractDatabase;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ForeignKeySnapshotGenerator extends JdbcSnapshotGenerator {

    public ForeignKeySnapshotGenerator() {
        super(ForeignKey.class, Table.class);
    }

//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException {
//        if (example instanceof ForeignKey) {
//            Database database = snapshot.getDatabase();
//            String searchCatalog = database.getJdbcCatalogName(example.getSchema());
//            String searchSchema = database.getJdbcSchemaName(example.getSchema());
//            String searchTableName = null;
//            if (((ForeignKey) example).getForeignKeyTable() != null) {
//                searchTableName = ((ForeignKey) example).getForeignKeyTable().getName();
//            }
//            String fkName = example.getName();
//
//            ResultSet rs = null;
//            try {
//                rs = getMetaData(database).getImportedKeys(searchCatalog, searchSchema, searchTableName);
//                while (rs.next()) {
//                    if (fkName.equals(rs.getString("FK_NAME"))) {
//                        return true;
//                    }
//                }
//                return false;
//            } catch (SQLException e) {
//                throw new DatabaseException(e);
//            } finally {
//                if (rs != null) {
//                    try {
//                        rs.close();
//                    } catch (SQLException ignored) { }
//                }
//            }
//        } else {
//            return chain.has(example, snapshot);
//        }
//    }


    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema;
            schema = table.getSchema();


            Set<String> seenFks = new HashSet<String>();
            ResultSet importedKeyMetadataResultSet = null;
            try {
                importedKeyMetadataResultSet = getMetaData(database).getImportedKeys(((AbstractDatabase) database).getJdbcCatalogName(schema), ((AbstractDatabase) database).getJdbcSchemaName(schema), database.correctObjectName(table.getName(), Table.class));

                while (importedKeyMetadataResultSet.next()) {
                    ForeignKey fk = new ForeignKey().setName(importedKeyMetadataResultSet.getString("FK_NAME")).setForeignKeyTable(table);
                    if (seenFks.add(fk.getName())) {
                        table.getOutgoingForeignKeys().add(snapshot.include(fk));
                    }
                }
            } catch (Exception e) {
                throw new DatabaseException(e);
            } finally {
                if (importedKeyMetadataResultSet != null) {
                    try {
                        importedKeyMetadataResultSet.close();
                    } catch (SQLException ignored) {
                    }
                }
            }

            seenFks = new HashSet<String>();
            ResultSet exportedKeyMetadataResultSet = null;
            try {
                exportedKeyMetadataResultSet = getMetaData(database).getExportedKeys(((AbstractDatabase) database).getJdbcCatalogName(schema), ((AbstractDatabase) database).getJdbcSchemaName(schema), database.correctObjectName(table.getName(), Table.class));

                while (exportedKeyMetadataResultSet.next()) {
                    ForeignKey fk = new ForeignKey().setName(exportedKeyMetadataResultSet.getString("FK_NAME")).setForeignKeyTable(table);
                    if (seenFks.add(fk.getName())) {
                        table.getIncomingForeignKeys().add(snapshot.include(fk));
                    }
                }
            } catch (Exception e) {
                throw new DatabaseException(e);
            } finally {
                if (exportedKeyMetadataResultSet != null) {
                    try {
                        exportedKeyMetadataResultSet.close();
                    } catch (SQLException ignored) {
                    }
                }
            }

        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {

        Database database = snapshot.getDatabase();

        ResultSet importedKeyMetadataResultSet = null;
        try {
            Table fkTable = ((ForeignKey) example).getForeignKeyTable();
            String searchCatalog = ((AbstractDatabase) database).getJdbcCatalogName(fkTable.getSchema());
            String searchSchema = ((AbstractDatabase) database).getJdbcSchemaName(fkTable.getSchema());
            String searchTableName = database.correctObjectName(fkTable.getName(), Table.class);

            importedKeyMetadataResultSet = getMetaData(database).getImportedKeys(searchCatalog, searchSchema, searchTableName);
            while (importedKeyMetadataResultSet.next()) {
                String fk_name = cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FK_NAME"), database);
                ForeignKey foreignKey = new ForeignKey();
                foreignKey.setName(fk_name);

                String fkTableCatalog = cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_CAT"), database);
                String fkTableSchema = cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_SCHEM"), database);
                String fkTableName = cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_NAME"), database);
                Table foreignKeyTable = new Table().setName(fkTableName);
                foreignKeyTable.setSchema(new Schema(new Catalog(fkTableCatalog), fkTableSchema));

                foreignKey.setForeignKeyTable(snapshot.include(foreignKeyTable));
                foreignKey.setForeignKeyColumns(cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FKCOLUMN_NAME"), database));

                CatalogAndSchema pkTableSchema = ((AbstractDatabase) database).getSchemaFromJdbcInfo(importedKeyMetadataResultSet.getString("PKTABLE_CAT"), importedKeyMetadataResultSet.getString("PKTABLE_SCHEM"));
                Table tempPkTable = (Table) new Table().setName(importedKeyMetadataResultSet.getString("PKTABLE_NAME")).setSchema(new Schema(pkTableSchema.getCatalogName(), pkTableSchema.getSchemaName()));
                foreignKey.setPrimaryKeyTable(snapshot.include(tempPkTable));
                foreignKey.setPrimaryKeyColumns(cleanNameFromDatabase(importedKeyMetadataResultSet.getString("PKCOLUMN_NAME"), database));
                //todo foreignKey.setKeySeq(importedKeyMetadataResultSet.getInt("KEY_SEQ"));

                ForeignKeyConstraintType updateRule = convertToForeignKeyConstraintType(importedKeyMetadataResultSet.getInt("UPDATE_RULE"), database);
                if (importedKeyMetadataResultSet.wasNull()) {
                    updateRule = null;
                }
                foreignKey.setUpdateRule(updateRule);
                ForeignKeyConstraintType deleteRule = convertToForeignKeyConstraintType(importedKeyMetadataResultSet.getInt("DELETE_RULE"), database);
                if (importedKeyMetadataResultSet.wasNull()) {
                    deleteRule = null;
                }
                foreignKey.setDeleteRule(deleteRule);
                short deferrability = importedKeyMetadataResultSet.getShort("DEFERRABILITY");
                if (deferrability == DatabaseMetaData.importedKeyInitiallyDeferred) {
                    foreignKey.setDeferrable(true);
                    foreignKey.setInitiallyDeferred(true);
                } else if (deferrability == DatabaseMetaData.importedKeyInitiallyImmediate) {
                    foreignKey.setDeferrable(true);
                    foreignKey.setInitiallyDeferred(false);
                } else if (deferrability == DatabaseMetaData.importedKeyNotDeferrable) {
                    foreignKey.setDeferrable(false);
                    foreignKey.setInitiallyDeferred(false);
                } else {
                    throw new RuntimeException("Unknown deferrablility result: " + deferrability);
                }

                return foreignKey;
            }
            return null;
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (importedKeyMetadataResultSet != null) {
                try {
                    importedKeyMetadataResultSet.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }


    protected ForeignKeyConstraintType convertToForeignKeyConstraintType(int jdbcType, Database database) throws DatabaseException {
        if (database instanceof MSSQLDatabase) {
            /*
                 * The sp_fkeys stored procedure spec says that returned integer values of 0, 1 and 2
     * translate to cascade, noAction and SetNull, which are not the values in the JDBC
     * standard. This override is a sticking plaster to stop invalid SQL from being generated.
             */
            if (jdbcType == 0) {
                return ForeignKeyConstraintType.importedKeyCascade;
            } else if (jdbcType == 1) {
                return ForeignKeyConstraintType.importedKeyNoAction;
            } else if (jdbcType == 2) {
                return ForeignKeyConstraintType.importedKeySetNull;
            } else {
                throw new DatabaseException("Unknown constraint type: " + jdbcType);
            }
        } else {
            if (jdbcType == DatabaseMetaData.importedKeyCascade) {
                return ForeignKeyConstraintType.importedKeyCascade;
            } else if (jdbcType == DatabaseMetaData.importedKeyNoAction) {
                return ForeignKeyConstraintType.importedKeyNoAction;
            } else if (jdbcType == DatabaseMetaData.importedKeyRestrict) {
                return ForeignKeyConstraintType.importedKeyRestrict;
            } else if (jdbcType == DatabaseMetaData.importedKeySetDefault) {
                return ForeignKeyConstraintType.importedKeySetDefault;
            } else if (jdbcType == DatabaseMetaData.importedKeySetNull) {
                return ForeignKeyConstraintType.importedKeySetNull;
            } else {
                throw new DatabaseException("Unknown constraint type: " + jdbcType);
            }
        }
    }

    //from SQLiteDatabaseSnapshotGenerator
    //    protected void readForeignKeys(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        updateListeners("Reading foreign keys for " + snapshot.getDatabase().toString() + " ...");
//        // Foreign keys are not supported in SQLite until now.
//        // ...do nothing here
//    }


    //From InformixDatabaseSnapshotGenerator
//	public List<ForeignKey> getForeignKeys(String schemaName, String foreignKeyTableName, Database database) throws DatabaseException {
//        List<ForeignKey> fkList = new ArrayList<ForeignKey>();
//		try {
//            String dbCatalog = database.convertRequestedSchemaToCatalog(schemaName);
//            // Informix handles schema differently
//            String dbSchema = null;
//            ResultSet rs = getMetaData(database).getImportedKeys(dbCatalog, dbSchema, database.correctTableName(foreignKeyTableName));
//
//            try {
//                while (rs.next()) {
//                    ForeignKeyInfo fkInfo = readForeignKey(rs);
//
//                    fkList.add(generateForeignKey(fkInfo, database, fkList));
//                }
//            } finally {
//                rs.close();
//            }
//
//            return fkList;
//
//        } catch (Exception e) {
//            throw new DatabaseException(e);
//        }
//    }

    //Code from OracleDatabaseSnapshotGenerator
//    public List<ForeignKey> getAdditionalForeignKeys(String schemaName, Database database) throws DatabaseException {
//        List<ForeignKey> foreignKeys = super.getAdditionalForeignKeys(schemaName, database);
//
//        // Setting default schema name. Needed for correct statement generation
//        if (schemaName == null) {
//            schemaName = database.convertRequestedSchemaToSchema(schemaName);
//        }
//
//        // Create SQL statement to select all FKs in database which referenced to unique columns
//        String query = "select uc_fk.constraint_name FK_NAME,uc_fk.owner FKTABLE_SCHEM,ucc_fk.table_name FKTABLE_NAME,ucc_fk.column_name FKCOLUMN_NAME,decode(uc_fk.deferrable, 'DEFERRABLE', 5 ,'NOT DEFERRABLE', 7 , 'DEFERRED', 6 ) DEFERRABILITY, decode(uc_fk.delete_rule, 'CASCADE', 0,'NO ACTION', 3) DELETE_RULE,ucc_rf.table_name PKTABLE_NAME,ucc_rf.column_name PKCOLUMN_NAME from all_cons_columns ucc_fk,all_constraints uc_fk,all_cons_columns ucc_rf,all_constraints uc_rf where uc_fk.CONSTRAINT_NAME = ucc_fk.CONSTRAINT_NAME and uc_fk.constraint_type='R' and uc_fk.r_constraint_name=ucc_rf.CONSTRAINT_NAME and uc_rf.constraint_name = ucc_rf.constraint_name and uc_rf.constraint_type = 'U' and uc_fk.owner = '" + schemaName + "' and ucc_fk.owner = '" + schemaName + "' and uc_rf.owner = '" + schemaName + "' and ucc_rf.owner = '" + schemaName + "'";
//        Statement statement = null;
//        ResultSet rs = null;
//        try {
//            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//            rs = statement.executeQuery(query);
//            while (rs.next()) {
//                ForeignKeyInfo fkInfo = new ForeignKeyInfo();
//                fkInfo.setReferencesUniqueColumn(true);
//                fkInfo.setFkName(cleanObjectNameFromDatabase(rs.getString("FK_NAME")));
//                fkInfo.setFkSchema(cleanObjectNameFromDatabase(rs.getString("FKTABLE_SCHEM")));
//                fkInfo.setFkTableName(cleanObjectNameFromDatabase(rs.getString("FKTABLE_NAME")));
//                fkInfo.setFkColumn(cleanObjectNameFromDatabase(rs.getString("FKCOLUMN_NAME")));
//
//                fkInfo.setPkTableName(cleanObjectNameFromDatabase(rs.getString("PKTABLE_NAME")));
//                fkInfo.setPkColumn(cleanObjectNameFromDatabase(rs.getString("PKCOLUMN_NAME")));
//
//                fkInfo.setDeferrablility(rs.getShort("DEFERRABILITY"));
//                ForeignKeyConstraintType deleteRule = convertToForeignKeyConstraintType(rs.getInt("DELETE_RULE"));
//                if (rs.wasNull()) {
//                    deleteRule = null;
//                }
//                fkInfo.setDeleteRule(deleteRule);
//                foreignKeys.add(generateForeignKey(fkInfo, database, foreignKeys));
//            }
//        } catch (SQLException e) {
//            throw new DatabaseException("Can't execute selection query to generate list of foreign keys", e);
//        } finally {
//            JdbcUtils.closeResultSet(rs);
//            JdbcUtils.closeStatement(statement);
//        }
//        return foreignKeys;
//    }
//

//
//
}
