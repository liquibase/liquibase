package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.jvm.ColumnMapRowMapper;
import liquibase.executor.jvm.RowMapperResultSetExtractor;
import liquibase.structure.core.Table;

import java.sql.*;
import java.util.*;

import liquibase.logging.LogFactory;

public class JdbcDatabaseSnapshot extends DatabaseSnapshot {
    private CachingDatabaseMetaData cachingDatabaseMetaData;
    private int tablesOfColumnsFetched = 0;

    public JdbcDatabaseSnapshot(SnapshotControl snapshotControl, Database database) {
        super(snapshotControl, database);
    }

    public JdbcDatabaseSnapshot(Database database) {
        super(database);
    }

    public CachingDatabaseMetaData getMetaData() throws SQLException {
        if (cachingDatabaseMetaData == null) {
            DatabaseMetaData databaseMetaData = null;
            if (getDatabase().getConnection() != null) {
                databaseMetaData = ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().getMetaData();
            }

            cachingDatabaseMetaData = new CachingDatabaseMetaData(databaseMetaData);
        }
        return cachingDatabaseMetaData;
    }

    public class CachingDatabaseMetaData {
        private DatabaseMetaData databaseMetaData;

        private Map<String, List<CachedRow>> cachedResults = new HashMap<String, List<CachedRow>>();

        public CachingDatabaseMetaData(DatabaseMetaData metaData) {
            this.databaseMetaData = metaData;
        }

        public DatabaseMetaData getDatabaseMetaData() {
            return databaseMetaData;
        }

        public List<CachedRow> getExportedKeys(String catalogName, String schemaName, String table) throws SQLException {
            String key = createKey("getExportedKeys", catalogName, schemaName, table);
            if (hasCachedValue(key)) {
                return getCachedValue(key);
            }
            return cacheResultSet(key, databaseMetaData.getExportedKeys(catalogName, schemaName, table));
        }

        private List<CachedRow> getCachedValue(String key) {
            return cachedResults.get(key);
        }

        private boolean hasCachedValue(String key) {
            return cachedResults.containsKey(key);
        }

        private String createKey(String methodName, Object... params) {
            String key = methodName;
            if (params != null) {
                for (Object param : params) {
                    key += ":"+param;
                }
            }
            return key;
        }

        private List<CachedRow> cacheResultSet(String key, ResultSet rs) throws SQLException {
            List list;
            try {
                list = (List) new RowMapperResultSetExtractor(new ColumnMapRowMapper()).extractData(rs);
                for (int i=0; i<list.size(); i++) {
                    list.set(i, new CachedRow((Map) list.get(i)));
                }
            } finally {
                rs.close();
            }

            cachedResults.put(key, list);
            return list;

        }

        public List<CachedRow> getImportedKeys(String catalogName, String schemaName, String table) throws SQLException {
            String key = createKey("getImportedKeys", catalogName, schemaName, table);
            if (hasCachedValue(key)) {
                return getCachedValue(key);
            }

            return cacheResultSet(key, databaseMetaData.getImportedKeys(catalogName, schemaName, table));
        }

        public List<CachedRow> getIndexInfo(String catalogName, String schemaName, String table, boolean unique, boolean approximate) throws SQLException {
            String key = createKey("getIndexInfo", catalogName, schemaName, table, unique, approximate);
            if (hasCachedValue(key)) {
                return getCachedValue(key);
            }

            return cacheResultSet(key, databaseMetaData.getIndexInfo(catalogName, schemaName, table, unique, approximate));
        }

        /**
         * Return the columns for the given catalog, schema, table, and column.
         * Because this query can be expensive on some databases (I'm looking at you, Oracle), after a couple calls for different tables it will give up and get all columns for all tables.
         * To override when this
         */
        public List<CachedRow> getColumns(String catalogName, String schemaName, String tableNamePattern, String columnNamePattern) throws SQLException {
            String byTableKey = createKey("getColumns", catalogName, schemaName, tableNamePattern);
            String allTablesKey = createKey("getColumns", catalogName, schemaName);
            List<CachedRow> returnList;

            boolean needToFilter = false;
            if (hasCachedValue(byTableKey)) {
                returnList = getCachedValue(byTableKey);
            } else if (hasCachedValue(allTablesKey)) {
                returnList = getCachedValue(allTablesKey);
                needToFilter = true;
            } else {
                tablesOfColumnsFetched++;

                if (shouldCacheAllTableColumns()) {
                    returnList = cacheResultSet(allTablesKey, databaseMetaData.getColumns(catalogName, schemaName, null, null));
                    needToFilter = true;
                } else {
                    returnList = cacheResultSet(byTableKey, databaseMetaData.getColumns(catalogName, schemaName, tableNamePattern, null));
                    if (returnList.size() == 0) {
                        throw new UnexpectedLiquibaseException("No Columns found for table "+tableNamePattern);
                    }
                }
            }

            if (columnNamePattern != null || needToFilter) {
                List<CachedRow> filteredList = new ArrayList<CachedRow>();
                for (CachedRow row : returnList) {
                    if (getDatabase().isCaseSensitive()) {
                        if (row.getString("TABLE_NAME").equals(tableNamePattern))
                            if (columnNamePattern == null || row.getString("COLUMN_NAME").equals(columnNamePattern)) {
                                filteredList.add(row);
                            }
                    } else {
                        if (row.getString("TABLE_NAME").equalsIgnoreCase(tableNamePattern))
                            if (columnNamePattern == null || row.getString("COLUMN_NAME").equalsIgnoreCase(columnNamePattern)) {
                                filteredList.add(row);
                            }
                    }
                }
                if (filteredList.size() == 0) {
                    LogFactory.getInstance().getLog().debug("zero size");
                }
                return filteredList;
            } else {
                if (returnList.size() == 0) {
                    LogFactory.getInstance().getLog().debug("zero size");
                }
                return returnList;
            }
        }

        public List<CachedRow> getTables(String catalogName, String schemaName, String tableNamePattern, String[] types) throws SQLException {
            String key = createKey("getTables", catalogName, schemaName, tableNamePattern, types);
            if (hasCachedValue(key)) {
                return getCachedValue(key);
            }

            return cacheResultSet(key, databaseMetaData.getTables(catalogName, schemaName, tableNamePattern, types));
        }

        public List<CachedRow> getPrimaryKeys(String catalogName, String schemaName, String table) throws SQLException {
            String key = createKey("getPrimaryKeys", catalogName, schemaName, table);
            if (hasCachedValue(key)) {
                return getCachedValue(key);
            }

            return cacheResultSet(key, databaseMetaData.getPrimaryKeys(catalogName, schemaName, table));
        }

        public List<CachedRow> query(String sql) throws SQLException {
            String key = createKey("query", sql);
            if (hasCachedValue(key)) {
                return getCachedValue(key);
            }

            Statement statement = this.getDatabaseMetaData().getConnection().createStatement();
            try {
                return cacheResultSet(key, statement.executeQuery(sql));
            } finally {
                statement.close();
            }
        }
    }

    protected boolean shouldCacheAllTableColumns() {
        return tablesOfColumnsFetched == 4;
    }

    public class CachedRow {
        private Map row;

        public CachedRow(Map row) {
            this.row = row;
        }



        public Object get(String columnName) {
            return row.get(columnName);
        }

        public void set(String columnName, Object value) {
            row.put(columnName, value);
        }


        public boolean containsColumn(String columnName) {
            return row.containsKey(columnName);
        }

        public String getString(String columnName) {
            return (String) row.get(columnName);
        }

        public Integer getInt(String columnName) {
            Object o = row.get(columnName);
            if (o instanceof Number) {
                return ((Number) o).intValue();
            } else if (o instanceof String) {
                return Integer.valueOf((String) o);
            }
            return (Integer) o;
        }

        public Short getShort(String columnName) {
            Object o = row.get(columnName);
            if (o instanceof Number) {
                return ((Number) o).shortValue();
            } else if (o instanceof String) {
                return Short.valueOf((String) o);
            }
            return (Short) o;
        }

        public Boolean getBoolean(String columnName) {
            Object o = row.get(columnName);
            if (o instanceof Number) {
                if (((Number) o).longValue() == 0) {
                    return false;
                } else {
                    return true;
                }
            }
            if (o instanceof String) {
                return Boolean.valueOf((String) o);
            }
            return (Boolean) o;
        }
    }

}
