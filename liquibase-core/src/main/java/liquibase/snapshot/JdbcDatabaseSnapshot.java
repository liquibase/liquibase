package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
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
                        for (CachedRow row : getTables(jdbcCatalogName, jdbcSchemaName, null, new String[] {"TABLE"})) {
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
                    if (database instanceof OracleDatabase) { //from https://community.oracle.com/thread/2563173
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
                                "FROM  " +
                                "  all_cons_columns pc,  " +
                                "  all_constraints p,  " +
                                "  all_cons_columns fc,  " +
                                "  all_constraints f  " +
                                "WHERE 1 = 1  " +
//                                "  AND p.table_name = '"+foundTable+"'  " +
//                                    "  AND f.table_name = :2  " +
                                "  AND p.owner = '"+jdbcSchemaName+"'  " +
//                                    "  AND f.owner = '"+jdbcSchemaName+"'  " +
                                "  AND f.constraint_type = 'R'  " +
                                "  AND p.owner = f.r_owner  " +
                                "  AND p.constraint_name = f.r_constraint_name  " +
                                "  AND p.constraint_type in ('P', 'U')  " +
                                "  AND pc.owner = p.owner  " +
                                "  AND pc.constraint_name = p.constraint_name  " +
                                "  AND pc.table_name = p.table_name  " +
                                "  AND fc.owner = f.owner  " +
                                "  AND fc.constraint_name = f.constraint_name  " +
                                "  AND fc.table_name = f.table_name  " +
                                "  AND fc.position = pc.position  " +
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
                                "WHERE c.TABLE_OWNER='" + database.correctObjectName(catalogAndSchema.getCatalogName(), Schema.class) + "'";
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


                @Override
                boolean shouldBulkSelect(String schemaKey, ResultSetCache resultSetCache) {
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
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    try {
	                    List<CachedRow> general_cache = extract(databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), tableName, columnName));
			            if (database instanceof MySQLDatabase) {
			                
			                //DatabaseSnapshot db_snapshot = DatabaseSnapshot( null, database);
			                
			                List<CachedRow> mysql_cache = executeAndExtract(createMySQLSql(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), tableName, columnName), JdbcDatabaseSnapshot.this.getDatabase());
			                int index = 0;
			                for (CachedRow row : general_cache) {
			                
			                    if ( mysql_cache.size() > 0 ) {

			                        for ( CachedRow MySQLrow : mysql_cache) {
			                            if( MySQLrow.getString("TABLE_NAME").equals( row.getString("TABLE_NAME") ) && MySQLrow.getString("COLUMN_NAME").equals( row.getString("COLUMN_NAME") )  ){
			                                row.set ( "COLUMN_TYPE", MySQLrow.getString("COLUMN_TYPE") );
			                                general_cache.set(index, row);
			                            }
			                        }

			                    }
			                    index++;
			                }
			                
			           	}
                    
                    	return general_cache;
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
                        List<CachedRow> general_cache = extract(databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null, null));
		                if (database instanceof MySQLDatabase) {
		                    
		                    List<CachedRow> mysql_cache = executeAndExtract(createMySQLSql(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null, null), JdbcDatabaseSnapshot.this.getDatabase());
		                    int index = 0;
		                    for (CachedRow row : general_cache) {
		                    
		                        if ( mysql_cache.size() > 0 ) {

		                            for ( CachedRow MySQLrow : mysql_cache) {
		                                if( MySQLrow.getString("TABLE_NAME").equals( row.getString("TABLE_NAME") ) && MySQLrow.getString("COLUMN_NAME").equals( row.getString("COLUMN_NAME") )  ){
		                                    row.set ( "COLUMN_TYPE", MySQLrow.getString("COLUMN_TYPE") );
		                                    general_cache.set(index, row);
		                                }
		                                
		                            }

		                        }
		                        index++;
		                    }
		                    
		               	}
		                
		                return general_cache;
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
                
                private String createMySQLSql(String catalogName, String schemaName, String tableName, String columnName) throws SQLException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    String jdbcCatalogName = database.correctObjectName(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), Catalog.class);
                    
                    Database database = JdbcDatabaseSnapshot.this.getDatabase();
                    String sql = "";
                    String andTable = "";
                    String andColumn = "";
                    
                    if( tableName != null   ){
                        andTable = " AND TABLE_NAME = '" + database.correctObjectName(tableName, Table.class) + "' ";
                    }
                    
                    if( columnName != null   ){
                        andColumn = " AND COLUMN_NAME = '" + columnName + "' ";
                    }
                    
                    if (database instanceof MySQLDatabase) {

                        sql = "SELECT * FROM "
                                + database.getSystemSchema() + ".COLUMNS WHERE TABLE_SCHEMA='"+ jdbcCatalogName + "'" + andTable + andColumn ;

                    }
                    
                    return sql;
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
				public List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

					List<CachedRow> table_cache = new ArrayList<CachedRow>();
                    List<CachedRow> new_cache = new ArrayList<CachedRow>();
                    String catalog = "";
                    String schema = "";
                    
                    if (database instanceof OracleDatabase) {
                        
                        table_cache = queryOracle(catalogAndSchema, null);
                        
                    }else{

                        catalog = ((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema);
                        schema = ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema);

                        table_cache = extract(databaseMetaData.getTables(catalog, schema, database.correctObjectName(table, Table.class), types));
                    
                    }
                    
                    if ( table_cache.size() > 0 ) {
                        for (CachedRow row : table_cache) {
                            
                            String tableName = StringUtils.trimToNull( row.getString("TABLE_NAME") );
                            
                            if( tableName != null ){
                                if ( database.includeTable( tableName ) ) {
                                    new_cache.add(row);
                                }
                            }
                            
                        }
                    }
                    
                    return new_cache;                    


                }


                @Override
				public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    List<CachedRow> table_cache = new ArrayList<CachedRow>();
                    List<CachedRow> new_cache = new ArrayList<CachedRow>();
                    String catalog = "";
                    String schema = "";
                    
                    if (database instanceof OracleDatabase) {
                        table_cache = queryOracle(catalogAndSchema, null);
                    }else{
                        table_cache = extract(databaseMetaData.getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), null, types));
                    }
                    
                    if ( table_cache.size() > 0 ) {
                        for (CachedRow row : table_cache) {
                            
                            String tableName = StringUtils.trimToNull( row.getString("TABLE_NAME") );
                            
                            if( tableName != null ){
                                if ( database.includeTable( tableName ) ) {
                                    new_cache.add(row);
                                }
                            }
                            
                        }
                    }

                    return new_cache;
                }

                private List<CachedRow> queryOracle(CatalogAndSchema catalogAndSchema, String tableName) throws DatabaseException, SQLException {
                    List<CachedRow> results = new ArrayList<CachedRow>();
                    for (String type : types) {
                        String allTable;
                        String nameColumn;
                        if (type.equalsIgnoreCase("table")) {
                            allTable = "ALL_TABLES";
                            nameColumn = "TABLE_NAME";
                        } else if (type.equalsIgnoreCase("view")) {
                            allTable = "ALL_VIEWS";
                            nameColumn = "VIEW_NAME";
                        } else {
                            throw new UnexpectedLiquibaseException("Unknown table type: "+type);
                        }

                        String ownerName = database.correctObjectName(catalogAndSchema.getCatalogName(), Schema.class);
                        String sql = "SELECT null as TABLE_CAT, a.OWNER as TABLE_SCHEM, a."+nameColumn+" as TABLE_NAME, 'TABLE' as TABLE_TYPE,  c.COMMENTS as REMARKS " +
                            "from "+allTable+" a " +
                            "join ALL_TAB_COMMENTS c on a."+nameColumn+"=c.table_name and a.owner=c.owner " +
                            "WHERE a.OWNER='" + ownerName + "'";
                        if (tableName != null) {
                            sql += " AND "+nameColumn+"='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                        sql += " AND a."+nameColumn+" not in (select mv.name from all_registered_mviews mv where mv.owner='"+ownerName+"')";
                        results.addAll(executeAndExtract(sql, database));
                    }
                    return results;
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
                    CatalogAndSchema catalogAndSchema = new CatalogAndSchema(catalogName, schemaName).customize(database);

                    return extract(databaseMetaData.getPrimaryKeys(((AbstractJdbcDatabase) database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), table));
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
                        sql = "select CONSTRAINT_NAME, TABLE_NAME " +
                                "from "+database.getSystemSchema()+".table_constraints " +
                                "where constraint_schema='" + jdbcCatalogName + "' " +
                                "and constraint_type='UNIQUE'";
                        if (tableName != null) {
                            sql += " and table_name='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof PostgresDatabase) {
                        sql = "select CONSTRAINT_NAME, TABLE_NAME " +
                                "from "+database.getSystemSchema()+".table_constraints " +
                                "where constraint_catalog='" + jdbcCatalogName + "' " +
                                "and constraint_schema='"+jdbcSchemaName+"' " +
                                "and constraint_type='UNIQUE'";
                        if (tableName != null) {
                                sql += " and table_name='" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof MSSQLDatabase) {
                        sql = "select CONSTRAINT_NAME, TABLE_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                                "where CONSTRAINT_TYPE = 'Unique' " +
                                "and CONSTRAINT_SCHEMA='"+jdbcSchemaName+"'";
                        if (tableName != null) {
                                sql += " and TABLE_NAME='"+database.correctObjectName(tableName, Table.class)+"'";
                        }
                    } else if (database instanceof OracleDatabase) {
                        sql = "select uc.constraint_name, uc.table_name,uc.status,uc.deferrable,uc.deferred,ui.tablespace_name from all_constraints uc, all_indexes ui " +
                                "where uc.constraint_type='U' and uc.index_name = ui.index_name " +
                                "and uc.owner = '" + jdbcSchemaName + "' " +
                                "and ui.table_owner = '" + jdbcSchemaName + "' ";
                        if (tableName != null) {
                            sql += " and uc.table_name = '" + database.correctObjectName(tableName, Table.class) + "'";
                        }
                    } else if (database instanceof DB2Database) {
                        sql = "select distinct k.constname as constraint_name, t.tabname as TABLE_NAME from syscat.keycoluse k, syscat.tabconst t " +
                                "where k.constname = t.constname " +
                                "and t.tabschema = '" + jdbcCatalogName + "' " +
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
                                "where s.schemaname='"+jdbcCatalogName+"' "+
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
                                "where constraint_schema='" + jdbcSchemaName + "' " +
                                "and constraint_catalog='" + jdbcCatalogName + "' " +
                                "and constraint_type='UNIQUE'";
                        if (tableName != null) {
                                sql += " and table_name='" + database.correctObjectName(tableName, Table.class) + "'";
                        }

                    }

                    return sql;
                }
            });
        }

		public List<CachedRow> getFulltextConstraints(final String catalogName, final String schemaName, final String tableName) throws SQLException, DatabaseException {
            return getResultSetCache("getFulltextConstraints").get(new ResultSetCache.SingleResultSetExtractor(database) {

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
                    String sql = "";

                    /* */
                    if (database instanceof MySQLDatabase) {

                        String andTableName = "";
                        if (tableName != null) {
                            andTableName += " AND TABLE_NAME = '" + database.correctObjectName(tableName, Table.class) + "' ";
                        }

                        sql = "SELECT INDEX_NAME AS CONSTRAINT_NAME, TABLE_NAME FROM     "
                                + database.getSystemSchema() + ".statistics WHERE    index_type LIKE 'FULLTEXT%' AND TABLE_SCHEMA='"
                                + jdbcCatalogName + "' " + andTableName;

                    }

                    //System.out.println(sql);
                    return sql;
                }
            });

        }
        
        
        public List<CachedRow> getMySQLTableEngine(final String catalogName, final String schemaName, final String tableName) throws SQLException, DatabaseException {
            return getResultSetCache("getMySQLTableEngine").get(new ResultSetCache.SingleResultSetExtractor(database) {

                @Override
                public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                    return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"), database, row.getString("TABLE_NAME") );
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
                   
                    Database database = JdbcDatabaseSnapshot.this.getDatabase();
                    String sql = "";

                    /* */
                    if (database instanceof MySQLDatabase) {

                        String andTableName = "";
                        if (tableName != null) {
                            andTableName += " AND TABLE_NAME = '" + database.correctObjectName(tableName, Table.class) + "' ";
                        }
                        
                        sql =   "SELECT * " +
                                    "FROM "+ database.getSystemSchema() + ".TABLES " +
                                    "WHERE    TABLE_SCHEMA='"+ jdbcCatalogName + "' " + andTableName;

                    }
                    
                    return sql;
                }
            });

        }

    }

}
