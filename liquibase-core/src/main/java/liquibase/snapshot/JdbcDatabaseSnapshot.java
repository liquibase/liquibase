package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.executor.jvm.ColumnMapRowMapper;
import liquibase.executor.jvm.RowMapperResultSetExtractor;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcDatabaseSnapshot extends DatabaseSnapshot {
    private CachingDatabaseMetaData cachingDatabaseMetaData;

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

        public List<CachedRow> getColumns(String catalogName, String schemaName, String tableNamePattern, String columnNamePattern) throws SQLException {
            String key = createKey("getColumns", catalogName, schemaName, tableNamePattern, columnNamePattern);
            if (hasCachedValue(key)) {
                return getCachedValue(key);
            }

            return cacheResultSet(key, databaseMetaData.getColumns(catalogName, schemaName, tableNamePattern, columnNamePattern));
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
            return (Boolean) o;
        }
    }

}
