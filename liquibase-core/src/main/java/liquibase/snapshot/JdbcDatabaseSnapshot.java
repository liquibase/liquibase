package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.sql.*;
import java.util.*;

import liquibase.logging.LogFactory;

public class JdbcDatabaseSnapshot extends DatabaseSnapshot {
    private CachingDatabaseMetaData cachingDatabaseMetaData;

    public JdbcDatabaseSnapshot(DatabaseObject[] examples, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        super(examples, database, snapshotControl);
    }

    public JdbcDatabaseSnapshot(DatabaseObject[] examples, Database database) throws DatabaseException, InvalidExampleException {
        super(examples, database);
    }

    public CachingDatabaseMetaData getMetaData() throws SQLException {
        if (cachingDatabaseMetaData == null) {
            DatabaseMetaData databaseMetaData = null;
            if (getDatabase().getConnection() != null) {
                databaseMetaData = ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().getMetaData();
            }

            cachingDatabaseMetaData = new CachingDatabaseMetaData(this.getDatabase(), databaseMetaData);
        }
        return cachingDatabaseMetaData;
    }

    public class CachingDatabaseMetaData {
        private DatabaseMetaData databaseMetaData;
        private Database database;

        public CachingDatabaseMetaData(Database database, DatabaseMetaData metaData) {
            this.databaseMetaData = metaData;
            this.database = database;
        }

        public DatabaseMetaData getDatabaseMetaData() {
            return databaseMetaData;
        }

        public List<CachedRow> getForeignKeys(final String catalogName, final String schemaName, final String tableName, final String fkName) throws DatabaseException {
            return getResultSetCache("getImportedKeys").get(new ResultSetCache.UnionResultSetExtractor(database) {


                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("FKTABLE_CAT"), row.getString("FKTABLE_SCHEM"), database, row.getString("FKTABLE_NAME"), row.getString("FK_NAME"));
                }


                @Override
				public ResultSetCache.RowData wantedKeyParameters() {
                    return new ResultSetCache.RowData(catalogName, schemaName, database, tableName, fkName);
                }


                @Override
				public List<CachedRow> fastFetch() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    List<CachedRow> returnList = new ArrayList<CachedRow>();

                    List<String> tables = new ArrayList<String>();
                    if (tableName == null) {
                        for (CachedRow row : getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null, new String[] {"TABLE"})) {
                            tables.add(row.getString("TABLE_NAME"));
                        }
                    } else {
                        tables.add(tableName);
                    }


                    for (String foundTable : tables) {
                        returnList.addAll(extract(databaseMetaData.getImportedKeys(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), foundTable)));
                    }

                    return returnList;
                }


                @Override
				public List<CachedRow> bulkFetch() throws SQLException, DatabaseException {
                    return null;
                }


                @Override
                boolean shouldBulkSelect(ResultSetCache resultSetCache) {
                    return false;
                }
            });
        }

        public List<CachedRow> getIndexInfo(final String catalogName, final String schemaName, final String tableName, final String indexName) throws DatabaseException {
            return getResultSetCache("getIndexInfo").get(new ResultSetCache.UnionResultSetExtractor(database) {


                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"), database, row.getString("TABLE_NAME"), row.getString("INDEX_NAME"));
                }


                @Override
				public ResultSetCache.RowData wantedKeyParameters() {
                    return new ResultSetCache.RowData(catalogName, schemaName, database, tableName, indexName);
                }


                @Override
				public List<CachedRow> fastFetch() throws SQLException, DatabaseException {
                    List<CachedRow> returnList = new ArrayList<CachedRow>();

                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));
                    if (database instanceof OracleDatabase) {
                        //oracle getIndexInfo is buggy and slow.  See Issue 1824548 and http://forums.oracle.com/forums/thread.jspa?messageID=578383&#578383
                        String sql = "SELECT c.INDEX_NAME, 3 AS TYPE, c.TABLE_NAME, c.COLUMN_NAME, c.COLUMN_POSITION AS ORDINAL_POSITION, e.COLUMN_EXPRESSION AS FILTER_CONDITION, case I.UNIQUENESS when 'UNIQUE' then 0 else 1 end as NON_UNIQUE " +
                                "FROM ALL_IND_COLUMNS c " +
                                "JOIN ALL_INDEXES i on i.index_name = c.index_name " +
                                "LEFT JOIN all_ind_expressions e on (e.column_position = c.column_position AND e.index_name = c.index_name) " +
                                "WHERE c.TABLE_OWNER='" + database.correctObjectName(catalogAndSchema.getCatalogName(), Schema.class) + "'";
                        if (tableName != null) {
                            sql += " AND c.TABLE_NAME='" + database.correctObjectName(tableName, Table.class) + "'";
                        }

                        if (indexName != null) {
                            sql += " AND c.INDEX_NAME='" + database.correctObjectName(indexName, Index.class) + "'";
                        }

                        sql += " ORDER BY c.INDEX_NAME, ORDINAL_POSITION";

                        returnList.addAll(executeAndExtract(sql, database));
                    } else {
                        List<String> tables = new ArrayList<String>();
                        if (tableName == null) {
                            for (CachedRow row : getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null, new String[] {"TABLE"})) {
                                tables.add(row.getString("TABLE_NAME"));
                            }
                        } else {
                            tables.add(tableName);
                        }


                        for (String tableName : tables) {
                            returnList.addAll(extract(databaseMetaData.getIndexInfo(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), tableName, false, true)));
                        }
                    }

                    return returnList;
                }


                @Override
				public List<CachedRow> bulkFetch() throws SQLException, DatabaseException {
                    return fastFetch();
                }


                @Override
                boolean shouldBulkSelect(ResultSetCache resultSetCache) {
                    if (database instanceof OracleDatabase) {
                        return super.shouldBulkSelect(resultSetCache);
                    }
                    return false;
                }
            });
        }

        /**
         * Return the columns for the given catalog, schema, table, and column.
         */
        public List<CachedRow> getColumns(final String catalogName, final String schemaName, final String tableName, final String columnName) throws SQLException, DatabaseException {
            return getResultSetCache("getColumns").get(new ResultSetCache.SingleResultSetExtractor(database) {


                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"), database, row.getString("TABLE_NAME"), row.getString("COLUMN_NAME"));
                }


                @Override
				public ResultSetCache.RowData wantedKeyParameters() {
                    return new ResultSetCache.RowData(catalogName, schemaName, database, tableName, columnName);
                }


                @Override
                boolean shouldBulkSelect(ResultSetCache resultSetCache) {
                    Set<String> seenTables = resultSetCache.getInfo("seenTables", Set.class);
                    if (seenTables == null) {
                        seenTables = new HashSet<String>();
                        resultSetCache.putInfo("seenTables", seenTables);
                    }

                    seenTables.add(catalogName+":"+schemaName+":"+tableName);
                    return seenTables.size() > 2;
                }


                @Override
				public List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException {
                    if (database instanceof OracleDatabase) {
                        return oracleQuery(false);
                    }
                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    return extract(databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), tableName, columnName));
                }


                @Override
				public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
                    if (database instanceof OracleDatabase) {
                        return oracleQuery(true);
                    }

                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    return extract(databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null, null));
                }

                protected List<CachedRow> oracleQuery(boolean bulk) throws DatabaseException, SQLException {
                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    String sql = "select NULL AS TABLE_CAT, ALL_TAB_COLUMNS.OWNER AS TABLE_SCHEM, 'NO' as IS_AUTOINCREMENT, ALL_COL_COMMENTS.COMMENTS AS REMARKS, ALL_TAB_COLUMNS.* FROM ALL_TAB_COLUMNS, ALL_COL_COMMENTS " +
                            "WHERE ALL_COL_COMMENTS.OWNER=ALL_TAB_COLUMNS.OWNER " +
                            "AND ALL_COL_COMMENTS.TABLE_NAME=ALL_TAB_COLUMNS.TABLE_NAME " +
                            "AND ALL_COL_COMMENTS.COLUMN_NAME=ALL_TAB_COLUMNS.COLUMN_NAME " +
                            "AND ALL_TAB_COLUMNS.OWNER='"+((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema)+"'";
                    if (!bulk) {
                        if (tableName != null) {
                            sql += " AND ALL_TAB_COLUMNS.TABLE_NAME='"+database.escapeObjectName(tableName, Table.class)+"'";
                        }
                        if (columnName != null) {
                            sql += " AND ALL_TAB_COLUMNS.COLUMN_NAME='"+database.escapeObjectName(columnName, Column.class)+"'";
                        }
                    }

                    return this.executeAndExtract(sql, database);
                }
            });
        }

        public List<CachedRow> getTables(final String catalogName, final String schemaName, final String table, final String[] types) throws SQLException, DatabaseException {
            return getResultSetCache("getTables."+StringUtils.join(types, ":")).get(new ResultSetCache.SingleResultSetExtractor(database) {


                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"), database, row.getString("TABLE_NAME"));
                }


                @Override
				public ResultSetCache.RowData wantedKeyParameters() {
                    return new ResultSetCache.RowData(catalogName, schemaName, database, table);
                }


                @Override
				public List<CachedRow> fastFetchQuery() throws SQLException {
                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    String catalog = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
                    String schema = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);
                    return extract(databaseMetaData.getTables(catalog, schema, database.correctObjectName(table, Table.class), types));
                }


                @Override
				public List<CachedRow> bulkFetchQuery() throws SQLException {
                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    return extract(databaseMetaData.getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null, types));
                }
            });
        }

        public List<CachedRow> getPrimaryKeys(final String catalogName, final String schemaName, final String table) throws SQLException, DatabaseException {
            return getResultSetCache("getPrimaryKeys").get(new ResultSetCache.SingleResultSetExtractor(database) {


                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"),  database, row.getString("TABLE_NAME"));
                }


                @Override
				public ResultSetCache.RowData wantedKeyParameters() {
                    return new ResultSetCache.RowData(catalogName, schemaName, database, table);
                }


                @Override
				public List<CachedRow> fastFetchQuery() throws SQLException {
                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    return extract(databaseMetaData.getPrimaryKeys(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), table));
                }


                @Override
				public List<CachedRow> bulkFetchQuery() throws SQLException {
                    return null;
                }


                @Override
                boolean shouldBulkSelect(ResultSetCache resultSetCache) {
                    return false;
                }
            });
        }

        public List<CachedRow> getUniqueConstraints(final String catalogName, final String schemaName, final String tableName) throws SQLException, DatabaseException {
            return getResultSetCache("getUniqueConstraints").get(new ResultSetCache.SingleResultSetExtractor(database) {


                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"), database, row.getString("TABLE_NAME"));
                }


                @Override
				public ResultSetCache.RowData wantedKeyParameters() {
                    return new ResultSetCache.RowData(catalogName, schemaName, database, tableName);
                }


                @Override
				public List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    return executeAndExtract(createSql(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), tableName), JdbcDatabaseSnapshot.this.getDatabase());
                }


                @Override
				public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = database.correctSchema(new CatalogAndSchema(catalogName, schemaName));

                    return executeAndExtract(createSql(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null), JdbcDatabaseSnapshot.this.getDatabase());
                }

                private String createSql(String catalogName, String schemaName, String tableName) throws SQLException {
                    Database database = JdbcDatabaseSnapshot.this.getDatabase();
                    String sql;
                    if (database instanceof MySQLDatabase || database instanceof HsqlDatabase) {
                        sql = "select CONSTRAINT_NAME, TABLE_NAME " +
                                "from "+database.getSystemSchema()+".table_constraints " +
                                "where constraint_schema='" + database.correctObjectName(catalogName, Catalog.class) + "' " +
                                "and constraint_type='UNIQUE'";
                        if (tableName != null) {
                            sql += " and table_name='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof PostgresDatabase) {
                        sql = "select CONSTRAINT_NAME, TABLE_NAME " +
                                "from "+database.getSystemSchema()+".table_constraints " +
                                "where constraint_catalog='" + database.correctObjectName(catalogName, Catalog.class) + "' " +
                                "and constraint_schema='"+database.correctObjectName(schemaName, Schema.class)+"' " +
                                "and constraint_type='UNIQUE'";
                        if (tableName != null) {
                                sql += " and table_name='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof MSSQLDatabase) {
                        sql = "select CONSTRAINT_NAME, TABLE_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                                "where CONSTRAINT_TYPE = 'Unique' " +
                                "and CONSTRAINT_SCHEMA='"+database.correctObjectName(schemaName, Schema.class)+"'";
                        if (tableName != null) {
                                sql += " and TABLE_NAME='"+database.correctObjectName(tableName, Table.class)+"'";
                        }
                    } else if (database instanceof OracleDatabase) {
                        sql = "select uc.constraint_name, uc.table_name,uc.status,uc.deferrable,uc.deferred,ui.tablespace_name from all_constraints uc, all_indexes ui " +
                                "where uc.constraint_type='U' and uc.index_name = ui.index_name " +
                                "and uc.owner = '" + database.correctObjectName(catalogName, Catalog.class) + "' " +
                                "and ui.table_owner = '" + database.correctObjectName(catalogName, Catalog.class) + "' ";
                        if (tableName != null) {
                            sql += " and uc.table_name = '" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof DB2iDatabase) {
                        sql = "select distinct CONSTRAINT_NAME from QSYS2.SYSCST " +
                                "where TABLE_SCHEMA = '" + database.correctObjectName(catalogName, Catalog.class) + "' " +
                                "and CONSTRAINT_TYPE = 'UNIQUE'";
                        if (tableName != null) {
                            sql += " and TABLE_NAME = '" + database.correctObjectName(tableName, Table.class) + "')";
                        }
                    } else if (database instanceof DB2Database) {
                        sql = "select distinct k.constname as constraint_name, t.tabname as TABLE_NAME from syscat.keycoluse k, syscat.tabconst t " +
                                "where k.constname = t.constname " +
                                "and t.tabschema = '" + database.correctObjectName(catalogName, Catalog.class) + "' " +
                                "and t.type='U'";
                        if (tableName != null) {
                            sql += " and t.tabname = '" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof FirebirdDatabase) {
                        sql = "SELECT RDB$INDICES.RDB$INDEX_NAME AS CONSTRAINT_NAME, RDB$INDICES.RDB$RELATION_NAME AS TABLE_NAME FROM RDB$INDICES " +
                                "LEFT JOIN RDB$RELATION_CONSTRAINTS ON RDB$RELATION_CONSTRAINTS.RDB$INDEX_NAME = RDB$INDICES.RDB$INDEX_NAME " +
                                "WHERE RDB$INDICES.RDB$UNIQUE_FLAG IS NOT NULL " +
                                "AND RDB$RELATION_CONSTRAINTS.RDB$CONSTRAINT_TYPE != 'PRIMARY KEY' "+
                                "AND NOT(RDB$INDICES.RDB$INDEX_NAME LIKE 'RDB$%')";
                        if (tableName != null) {
                            sql += " AND RDB$INDICES.RDB$RELATION_NAME='"+database.correctObjectName(tableName, Table.class)+"'";
                        }
                    } else if (database instanceof DerbyDatabase) {
                        sql = "select c.constraintname as CONSTRAINT_NAME, tablename AS TABLE_NAME " +
                                "from sys.systables t, sys.sysconstraints c, sys.sysschemas s " +
                                "where s.schemaname='"+database.correctObjectName(catalogName, Catalog.class)+"' "+
                                "and t.tableid = c.tableid " +
                                "and t.schemaid=s.schemaid " +
                                "and c.type = 'U'";
                        if (tableName != null) {
                            sql += " AND t.tablename = '"+database.correctObjectName(tableName, Table.class)+"'";
                        }
                    } else if (database instanceof InformixDatabase) {
                        sql = "select sysindexes.idxname, sysindexes.idxtype, systables.tabname "+
                        		"from sysindexes, systables "+
                        		"where sysindexes.tabid = systables.tabid "+
                        		"and sysindexes.idxtype ='U'";
                        if (tableName != null) {
                            sql += " AND systables.tabname = '"+database.correctObjectName(tableName, Table.class)+"'";
                        }
                    } else if (database instanceof SybaseDatabase) {
                        LogFactory.getLogger().warning("Finding unique constraints not currently supported for Sybase");
                        return null; //TODO: find sybase sql
                    } else {
                        sql = "select CONSTRAINT_NAME, CONSTRAINT_TYPE, TABLE_NAME " +
                                "from "+database.getSystemSchema()+".constraints " +
                                "where constraint_schema='" + database.correctObjectName(schemaName, Schema.class) + "' " +
                                "and constraint_catalog='" + database.correctObjectName(catalogName, Catalog.class) + "' " +
                                "and constraint_type='UNIQUE'";
                        if (tableName != null) {
                                sql += " and table_name='" + database.correctObjectName(tableName, Table.class) + "'";
                        }

                    }

                    return sql;
                }
            });
        }
    }

}
