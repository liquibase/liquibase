package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.sql.*;
import java.util.*;

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
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    List<CachedRow> returnList = new ArrayList<CachedRow>();

                    List<String> tables = new ArrayList<String>();
                    String jdbcCatalogName = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
                    String jdbcSchemaName = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);
                    if (tableName == null) {
                        for (CachedRow row : getTables(jdbcCatalogName, jdbcSchemaName, null)) {
                            tables.add(row.getString("TABLE_NAME"));
                        }
                    } else {
                        tables.add(tableName);
                    }

                    for (String foundTable : tables) {
                        if (database instanceof OracleDatabase) {
                            throw new RuntimeException("Should have bulk selected");
                        } else {
                            returnList.addAll(extract(databaseMetaData.getImportedKeys(jdbcCatalogName, jdbcSchemaName, foundTable)));
                        }
                    }

                    return returnList;
                }

                @Override
				public List<CachedRow> bulkFetch() throws SQLException, DatabaseException {
                    if (database instanceof OracleDatabase) {
                        CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                        String jdbcSchemaName = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);

                        String sql = "SELECT  " +
                                "  NULL AS pktable_cat,  " +
                                "  p.owner as pktable_schem,  " +
                                "  p.table_name as pktable_name,  " +
                                "  pc.column_name as pkcolumn_name,  " +
                                "  NULL as fktable_cat,  " +
                                "  f.owner as fktable_schem,  " +
                                "  f.table_name as fktable_name,  " +
                                "  fc.column_name as fkcolumn_name,  " +
                                "  fc.position as key_seq,  " +
                                "  NULL as update_rule,  " +
                                "  decode (f.delete_rule, 'CASCADE', 0, 'SET NULL', 2, 1) as delete_rule,  " +
                                "  f.constraint_name as fk_name,  " +
                                "  p.constraint_name as pk_name,  " +
                                "  decode(f.deferrable, 'DEFERRABLE', 5, 'NOT DEFERRABLE', 7, 'DEFERRED', 6) deferrability  " +
                                "FROM " +
                                "all_constraints p " +
                                "INNER JOIN all_cons_columns pc " +
                                "ON pc.owner = p.owner " +
                                "AND pc.constraint_name = p.constraint_name " +
                                "AND pc.table_name = p.table_name " +
                                "INNER JOIN all_constraints f " +
                                "ON p.owner = f.r_owner " +
                                "AND p.constraint_name = f.r_constraint_name " +
                                "INNER JOIN all_cons_columns fc " +
                                "ON fc.owner = f.owner " +
                                "AND fc.constraint_name = f.constraint_name " +
                                "AND fc.table_name = f.table_name " +
                                "AND fc.position = pc.position " +
                                "WHERE p.owner = '" +jdbcSchemaName+"' "+
                                "AND p.constraint_type in ('P', 'U') " +
                                "AND f.constraint_type = 'R' " +
                                "AND p.table_name NOT LIKE 'BIN$%' " +
                                "ORDER BY fktable_schem, fktable_name, key_seq";
                        return executeAndExtract(sql, database);
                    } else {
                        throw new RuntimeException("Cannot bulk select");
                    }
                }

                @Override
                boolean shouldBulkSelect(String schemaKey, ResultSetCache resultSetCache) {
                    return database instanceof OracleDatabase; //oracle is slow, always bulk select while you are at it. Other databases need to go through all tables.
                }
            });
        }

        public List<CachedRow> getIndexInfo(final String catalogName, final String schemaName, final String tableName, final String indexName) throws DatabaseException {
            return getResultSetCache("getIndexInfo").get(new ResultSetCache.UnionResultSetExtractor(database) {

                public boolean bulkFetch = false;

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

                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);
                    if (database instanceof OracleDatabase) {
                        //oracle getIndexInfo is buggy and slow.  See Issue 1824548 and http://forums.oracle.com/forums/thread.jspa?messageID=578383&#578383
                        String sql = "SELECT c.INDEX_NAME, 3 AS TYPE, c.TABLE_NAME, c.COLUMN_NAME, c.COLUMN_POSITION AS ORDINAL_POSITION, e.COLUMN_EXPRESSION AS FILTER_CONDITION, case I.UNIQUENESS when 'UNIQUE' then 0 else 1 end as NON_UNIQUE " +
                                "FROM ALL_IND_COLUMNS c " +
                                "JOIN ALL_INDEXES i on i.index_name = c.index_name " +
                                "LEFT JOIN all_ind_expressions e on (e.column_position = c.column_position AND e.index_name = c.index_name) " +
                                "WHERE c.TABLE_OWNER='" + database.correctObjectName(catalogAndSchema.getCatalogName(), Schema.class) + "' " +
                                "AND i.OWNER=c.TABLE_OWNER";
                        if (!bulkFetch && tableName != null) {
                            sql += " AND c.TABLE_NAME='" + database.correctObjectName(tableName, Table.class) + "'";
                        }

                        if (!bulkFetch && indexName != null) {
                            sql += " AND c.INDEX_NAME='" + database.correctObjectName(indexName, Index.class) + "'";
                        }

                        sql += " ORDER BY c.INDEX_NAME, ORDINAL_POSITION";

                        returnList.addAll(executeAndExtract(sql, database));
                    } else {
                        List<String> tables = new ArrayList<String>();
                        if (tableName == null) {
                            for (CachedRow row : getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null)) {
                                tables.add(row.getString("TABLE_NAME"));
                            }
                        } else {
                            tables.add(tableName);
                        }

                        for (String tableName : tables) {
                            ResultSet rs = databaseMetaData.getIndexInfo(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), tableName, false, true);
                            List<CachedRow> rows = extract(rs, (database instanceof InformixDatabase));
                            returnList.addAll(rows);
                        }
                    }

                    return returnList;
                }

                @Override
                public List<CachedRow> bulkFetch() throws SQLException, DatabaseException {
                    this.bulkFetch = true;
                    return fastFetch();
                }

                @Override
                boolean shouldBulkSelect(String schemaKey, ResultSetCache resultSetCache) {
                    if (database instanceof OracleDatabase) {
                        return super.shouldBulkSelect(schemaKey, resultSetCache);
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

                //
                // DVONE-2188 Always return true for shouldBulkSelect to fix problem with
                // missing columns in snapshot/baseline
                //
                @Override
                boolean shouldBulkSelect(String schemaKey, ResultSetCache resultSetCache) {
                    //Set<String> seenTables = resultSetCache.getInfo("seenTables", Set.class);
                    //if (seenTables == null) {
                    //    seenTables = new HashSet<String>();
                    //    resultSetCache.putInfo("seenTables", seenTables);
                    //}

                    //seenTables.add(catalogName + ":" + schemaName + ":" + tableName);
                    //return seenTables.size() > 2;
                    return true;
                }

                @Override
                public List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException {
                    if (database instanceof OracleDatabase) {
                        return oracleQuery(false);
                    }
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    try {
                        return extract(databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), tableName, null));
                    } catch (SQLException e) {
                        if (shouldReturnEmptyColumns(e)) { //view with table already dropped. Act like it has no columns.
                            return new ArrayList<CachedRow>();
                        } else {
                            throw e;
                        }
                    }
                }

                @Override
                public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
                    if (database instanceof OracleDatabase) {
                        return oracleQuery(true);
                    }

                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    try {
                        return extract(databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null, null));
                    } catch (SQLException e) {
                        if (shouldReturnEmptyColumns(e)) {
                            return new ArrayList<CachedRow>();
                        } else {
                            throw e;
                        }
                    }
                }

                protected boolean shouldReturnEmptyColumns(SQLException e) {
                    return e.getMessage().contains("references invalid table"); //view with table already dropped. Act like it has no columns.
                }

                protected List<CachedRow> oracleQuery(boolean bulk) throws DatabaseException, SQLException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    String sql = "select NULL AS TABLE_CAT, OWNER AS TABLE_SCHEM, 'NO' as IS_AUTOINCREMENT, cc.COMMENTS AS REMARKS,\n" +
                            "OWNER, TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_TYPE_MOD, DATA_TYPE_OWNER, " +
                            // note: oracle reports DATA_LENGTH=4*CHAR_LENGTH when using VARCHAR( <N> CHAR ), thus BYTEs
                            "DECODE( CHAR_USED, 'C',CHAR_LENGTH, DATA_LENGTH ) as DATA_LENGTH, " +
                            "DATA_PRECISION, DATA_SCALE, NULLABLE, COLUMN_ID, DEFAULT_LENGTH, " +
                            "DATA_DEFAULT, NUM_DISTINCT, LOW_VALUE, HIGH_VALUE, DENSITY, NUM_NULLS, " +
                            "NUM_BUCKETS, LAST_ANALYZED, SAMPLE_SIZE, CHARACTER_SET_NAME, " +
                            "CHAR_COL_DECL_LENGTH, GLOBAL_STATS, USER_STATS, AVG_COL_LEN, CHAR_LENGTH, " +
                            "CHAR_USED, V80_FMT_IMAGE, DATA_UPGRADED, VIRTUAL_COLUMN\n" +
                            "FROM ALL_TAB_COLS c " +
                            "JOIN ALL_COL_COMMENTS cc USING ( OWNER, TABLE_NAME, COLUMN_NAME ) " +
                            "WHERE OWNER='"+((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema)+"' AND hidden_column='NO'";
                    if (!bulk) {
                        if (tableName != null) {
                            sql += " AND TABLE_NAME='"+database.escapeObjectName(tableName, Table.class)+"'";
                        }
                        if (columnName != null) {
                            sql += " AND COLUMN_NAME='"+database.escapeObjectName(columnName, Column.class)+"'";
                        }
                    }
                    sql += " ORDER BY OWNER, TABLE_NAME, c.COLUMN_ID";

                    return this.executeAndExtract(sql, database);
                }
            });
        }

        public List<CachedRow> getTables(final String catalogName, final String schemaName, final String table) throws SQLException, DatabaseException {
            return getResultSetCache("getTables").get(new ResultSetCache.SingleResultSetExtractor(database) {

                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"), database, row.getString("TABLE_NAME"));
                }

                @Override
                public ResultSetCache.RowData wantedKeyParameters() {
                    return new ResultSetCache.RowData(catalogName, schemaName, database, table);
                }

                @Override
                public List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    if (database instanceof OracleDatabase) {
                        return queryOracle(catalogAndSchema, table);
                    }

                    String catalog = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
                    String schema = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);
                    return extract(databaseMetaData.getTables(catalog, schema, database.correctObjectName(table, Table.class), new String[]{"TABLE"}));
                }

                @Override
                public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    if (database instanceof OracleDatabase) {
                        return queryOracle(catalogAndSchema, null);
                    }

                    String catalog = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
                    String schema = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);
                    return extract(databaseMetaData.getTables(catalog, schema, null, new String[]{"TABLE"}));
                }

                private List<CachedRow> queryOracle(CatalogAndSchema catalogAndSchema, String tableName) throws DatabaseException, SQLException {
                    String ownerName = database.correctObjectName(catalogAndSchema.getCatalogName(), Schema.class);
                    
                    String sql = "SELECT null as TABLE_CAT, a.OWNER as TABLE_SCHEM, a.TABLE_NAME as TABLE_NAME, a.TEMPORARY as TEMPORARY, a.DURATION as DURATION, 'TABLE' as TABLE_TYPE, c.COMMENTS as REMARKS " +
                        "from ALL_TABLES a " +
                        "join ALL_TAB_COMMENTS c on a.TABLE_NAME=c.table_name and a.owner=c.owner " +
                        "WHERE a.OWNER='" + ownerName + "'";
                    if (tableName != null) {
                        sql += " AND a.TABLE_NAME='" + database.correctObjectName(tableName, Table.class) + "'";
                    }

                    return executeAndExtract(sql, database);
                }
            });
        }
		
        public List<CachedRow> getViews(final String catalogName, final String schemaName, final String view) throws SQLException, DatabaseException {
            return getResultSetCache("getViews").get(new ResultSetCache.SingleResultSetExtractor(database) {


                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"), database, row.getString("TABLE_NAME"));
                }


                @Override
				public ResultSetCache.RowData wantedKeyParameters() {
                    return new ResultSetCache.RowData(catalogName, schemaName, database, view);
                }


                @Override
				public List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    if (database instanceof OracleDatabase) {
                        return queryOracle(catalogAndSchema, view);
                    }

                    String catalog = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
                    String schema = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);
                    return extract(databaseMetaData.getTables(catalog, schema, database.correctObjectName(view, View.class), new String[]{"VIEW"}));
                }


                @Override
				public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    if (database instanceof OracleDatabase) {
                        return queryOracle(catalogAndSchema, null);
                    }

                    String catalog = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
                    String schema = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);
                    return extract(databaseMetaData.getTables(catalog, schema, null, new String[]{"VIEW"}));
                }

                private List<CachedRow> queryOracle(CatalogAndSchema catalogAndSchema, String viewName) throws DatabaseException, SQLException {
                    String ownerName = database.correctObjectName(catalogAndSchema.getCatalogName(), Schema.class);
                    
                    String sql = "SELECT null as TABLE_CAT, a.OWNER as TABLE_SCHEM, a.VIEW_NAME as TABLE_NAME, 'TABLE' as TABLE_TYPE, c.COMMENTS as REMARKS " +
                        "from ALL_VIEWS a " +
                        "join ALL_TAB_COMMENTS c on a.VIEW_NAME=c.table_name and a.owner=c.owner " +
                        "WHERE a.OWNER='" + ownerName + "'";
                    if (viewName != null) {
                        sql += " AND a.VIEW_NAME='" + database.correctObjectName(viewName, View.class) + "'";
                    }
                    sql += " AND a.VIEW_NAME not in (select mv.name from all_registered_mviews mv where mv.owner='"+ownerName+"')";

                    return executeAndExtract(sql, database);
                }
            });
        }

        public List<CachedRow> getPrimaryKeys(final String catalogName, final String schemaName, final String table) throws SQLException, DatabaseException {
            return getResultSetCache("getPrimaryKeys").get(new ResultSetCache.SingleResultSetExtractor(database) {

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
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    if (table == null) {
                        try {
                            List<CachedRow> foundPks = new ArrayList<CachedRow>();
                            List<CachedRow> tables = CachingDatabaseMetaData.this.getTables(catalogName, schemaName, null);
                            for (CachedRow table : tables) {
                                List<CachedRow> pkInfo = extract(databaseMetaData.getPrimaryKeys(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), table.getString("TABLE_NAME")));
                                if (pkInfo != null) {
                                    foundPks.addAll(pkInfo);
                                }
                            }
                            return foundPks;
                        } catch (DatabaseException e) {
                            throw new SQLException(e);
                        }
                    } else {
                        return extract(databaseMetaData.getPrimaryKeys(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), table));
                    }
                }

                @Override
                public List<CachedRow> bulkFetchQuery() throws SQLException {
                    return null;
                }

                @Override
                boolean shouldBulkSelect(String schemaKey, ResultSetCache resultSetCache) {
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
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    return executeAndExtract(createSql(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), tableName), JdbcDatabaseSnapshot.this.getDatabase());
                }

                @Override
                public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    return executeAndExtract(createSql(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null), JdbcDatabaseSnapshot.this.getDatabase());
                }

                private String createSql(String catalogName, String schemaName, String tableName) throws SQLException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    String jdbcCatalogName = database.correctObjectName(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), Catalog.class);
                    String jdbcSchemaName = database.correctObjectName(((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), Schema.class);

                    Database database = JdbcDatabaseSnapshot.this.getDatabase();
                    String sql;
                    if (database instanceof MySQLDatabase || database instanceof HsqlDatabase) {
                        sql = "select CONSTRAINT_NAME, TABLE_NAME "
                                + "from " + database.getSystemSchema() + ".table_constraints "
                                + "where constraint_schema='" + jdbcCatalogName + "' "
                                + "and constraint_type='UNIQUE'";
                        if (tableName != null) {
                            sql += " and table_name='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof PostgresDatabase) {
                        sql = "select CONSTRAINT_NAME, TABLE_NAME "
                                + "from " + database.getSystemSchema() + ".table_constraints "
                                + "where constraint_catalog='" + jdbcCatalogName + "' "
                                + "and constraint_schema='" + jdbcSchemaName + "' "
                                + "and constraint_type='UNIQUE'";
                        if (tableName != null) {
                            sql += " and table_name='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof MSSQLDatabase) {
                        sql = "select CONSTRAINT_NAME, TABLE_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                                + "where CONSTRAINT_TYPE = 'Unique' "
                                + "and CONSTRAINT_SCHEMA='" + jdbcSchemaName + "'";
                        if (tableName != null) {
                            sql += " and TABLE_NAME='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof OracleDatabase) {
                        sql = "select uc.constraint_name, uc.table_name,uc.status,uc.deferrable,uc.deferred,ui.tablespace_name from all_constraints uc, all_indexes ui "
                                + "where uc.constraint_type='U' and uc.index_name = ui.index_name "
                                + "and uc.owner = '" + jdbcSchemaName + "' "
                                + "and ui.table_owner = '" + jdbcSchemaName + "' ";
                        if (tableName != null) {
                            sql += " and uc.table_name = '" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof DB2Database) {
                        // if we are on DB2 AS400 iSeries
                        if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                            sql = "select constraint_name as constraint_name, table_name as table_name from QSYS2.TABLE_CONSTRAINTS where table_schema='" + jdbcSchemaName + "' and constraint_type='UNIQUE'";
                            if (tableName != null) {
                                sql += " and table_name = '" + database.correctObjectName(tableName, Table.class) + "'";
                            }
                        }
                        // here we are on DB2 UDB
                        else {
                            sql = "select distinct k.constname as constraint_name, t.tabname as TABLE_NAME from syscat.keycoluse k, syscat.tabconst t "
                                    + "where k.constname = t.constname "
                                    + "and t.tabschema = '" + jdbcCatalogName + "' "
                                    + "and t.type='U'";
                            if (tableName != null) {
                                sql += " and t.tabname = '" + database.correctObjectName(tableName, Table.class) + "'";
                            }
                        }
                    } else if (database instanceof FirebirdDatabase) {
                        sql = "SELECT RDB$INDICES.RDB$INDEX_NAME AS CONSTRAINT_NAME, RDB$INDICES.RDB$RELATION_NAME AS TABLE_NAME FROM RDB$INDICES "
                                + "LEFT JOIN RDB$RELATION_CONSTRAINTS ON RDB$RELATION_CONSTRAINTS.RDB$INDEX_NAME = RDB$INDICES.RDB$INDEX_NAME "
                                + "WHERE RDB$INDICES.RDB$UNIQUE_FLAG IS NOT NULL "
                                + "AND RDB$RELATION_CONSTRAINTS.RDB$CONSTRAINT_TYPE != 'PRIMARY KEY' "
                                + "AND NOT(RDB$INDICES.RDB$INDEX_NAME LIKE 'RDB$%')";
                        if (tableName != null) {
                            sql += " AND RDB$INDICES.RDB$RELATION_NAME='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof DerbyDatabase) {
                        sql = "select c.constraintname as CONSTRAINT_NAME, tablename AS TABLE_NAME "
                                + "from sys.systables t, sys.sysconstraints c, sys.sysschemas s "
                                + "where s.schemaname='" + jdbcCatalogName + "' "
                                + "and t.tableid = c.tableid "
                                + "and t.schemaid=s.schemaid "
                                + "and c.type = 'U'";
                        if (tableName != null) {
                            sql += " AND t.tablename = '" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof InformixDatabase) {
                        sql = "select sysindexes.idxname, sysindexes.idxtype, systables.tabname "
                                + "from sysindexes, systables "
                                + "where sysindexes.tabid = systables.tabid "
                                + "and sysindexes.idxtype ='U'";
                        if (tableName != null) {
                            sql += " AND systables.tabname = '" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof SybaseDatabase) {
                        LogFactory.getLogger().warning("Finding unique constraints not currently supported for Sybase");
                        return null; //TODO: find sybase sql
                    } else if (database instanceof SybaseASADatabase) {
                        sql = "select sysconstraint.constraint_name, sysconstraint.constraint_type, systable.table_name " +
                                "from sysconstraint, systable " +
                                "where sysconstraint.table_object_id = systable.object_id " +
                                "and sysconstraint.constraint_type = 'U'";
                        if (tableName != null) {
                            sql += " and systable.table_name = '" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else {
                        sql = "select CONSTRAINT_NAME, CONSTRAINT_TYPE, TABLE_NAME "
                                + "from " + database.getSystemSchema() + ".constraints "
                                + "where constraint_schema='" + jdbcSchemaName + "' "
                                + "and constraint_catalog='" + jdbcCatalogName + "' "
                                + "and constraint_type='UNIQUE'";
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
