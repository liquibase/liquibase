package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ForeignKeySnapshotGenerator extends JdbcDatabaseObjectSnapshotGenerator<ForeignKey> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean has(ForeignKey example, Database database) throws DatabaseException {
        return snapshot(example, database) != null;
    }

    public ForeignKey[] get(DatabaseObject container, Database database) throws DatabaseException {
        Schema schema;
        Table relation = null;
        if (container instanceof Schema) {
            schema = (Schema) container;
        } else if (container instanceof Table) {
            relation = (Table) container;
            schema = relation.getSchema();
        } else {
            return new ForeignKey[0];
        }

        updateListeners("Reading foreign keys for " + database.toString() + " ...");

        List<ForeignKey> returnList = new ArrayList<ForeignKey>();
        ResultSet importedKeyMetadataResultSet = null;
        try {
            List<String> tables = new ArrayList<String>();
            if (relation == null) {
                tables.addAll(listAllTables(new CatalogAndSchema(schema.getCatalogName(), schema.getName()), database));
            } else {
                tables.add(relation.getName());
            }

            for (String tableName : tables) {
                importedKeyMetadataResultSet = getMetaData(database).getImportedKeys(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), tableName);

                while (importedKeyMetadataResultSet.next()) {
                    ForeignKey newFk = readForeignKey(importedKeyMetadataResultSet, database);

                    if (newFk != null) {
                        returnList.add(newFk);
                    }
                }

            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (importedKeyMetadataResultSet != null) {
                try {
                    importedKeyMetadataResultSet.close();
                } catch (SQLException ignored) { }
            }
        }
        return returnList.toArray(new ForeignKey[returnList.size()]);
    }

    public ForeignKey snapshot(ForeignKey example, Database database) throws DatabaseException {
        String objectName = database.correctObjectName(example.getName(), ForeignKey.class);
        for (ForeignKey key : get(example.getSchema(), database)) {
            if (key.getName().equals(objectName)) {
                return key;
            }
        }

        return null;
    }

    protected ForeignKey readForeignKey(ResultSet importedKeyMetadataResultSet, Database database) throws DatabaseException, SQLException {
        String fk_name = cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FK_NAME"), database);
        ForeignKey foreignKey = new ForeignKey();
        foreignKey.setName(fk_name);

        String fkTableCatalog = cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_CAT"), database);
        String fkTableSchema = cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_SCHEM"), database);
        String fkTableName = cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_NAME"), database);
        Table foreignKeyTable = new Table().setName(fkTableName);
        foreignKeyTable.setSchema(new Schema(new Catalog(fkTableCatalog), fkTableSchema));

        foreignKey.setForeignKeyTable(foreignKeyTable);
        foreignKey.setForeignKeyColumns(cleanNameFromDatabase(importedKeyMetadataResultSet.getString("FKCOLUMN_NAME"), database));

        CatalogAndSchema pkTableSchema = database.getSchemaFromJdbcInfo(importedKeyMetadataResultSet.getString("PKTABLE_CAT"), importedKeyMetadataResultSet.getString("PKTABLE_SCHEM"));
        Table tempPkTable = (Table) new Table().setName(importedKeyMetadataResultSet.getString("PKTABLE_NAME")).setSchema(new Schema(pkTableSchema.getCatalogName(), pkTableSchema.getSchemaName()));
        foreignKey.setPrimaryKeyTable(tempPkTable);
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
