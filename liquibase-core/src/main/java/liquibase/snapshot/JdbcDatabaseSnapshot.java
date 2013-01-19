package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.executor.jvm.ColumnMapRowMapper;
import liquibase.executor.jvm.RowMapperResultSetExtractor;

import java.sql.*;
import java.util.Collection;
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

        public CachingDatabaseMetaData(DatabaseMetaData metaData) {
            this.databaseMetaData = metaData;
        }

        public DatabaseMetaData getDatabaseMetaData() {
            return databaseMetaData;
        }

        public List<CachedRow> getExportedKeys(String catalogName, String schemaName, String table) throws SQLException {
            return cacheResultSet(databaseMetaData.getExportedKeys(catalogName, schemaName, table));
        }

        public List<CachedRow> getImportedKeys(String catalogName, String schemaName, String table) throws SQLException {
            return cacheResultSet(databaseMetaData.getImportedKeys(catalogName, schemaName, table));
        }

        public List<CachedRow> getIndexInfo(String catalogName, String schemaName, String table, boolean unique, boolean approximate) throws SQLException {
            return cacheResultSet(databaseMetaData.getIndexInfo(catalogName, schemaName, table, unique, approximate));
        }

        public List<CachedRow> getColumns(String catalogName, String schemaName, String tableNamePattern, String columnNamePattern) throws SQLException {
            return cacheResultSet(databaseMetaData.getColumns(catalogName, schemaName, tableNamePattern, columnNamePattern));
        }

        public List<CachedRow> getTables(String catalogName, String schemaName, String tableNamePattern, String[] types) throws SQLException {
            return cacheResultSet(databaseMetaData.getTables(catalogName, schemaName, tableNamePattern, types));
        }

        public List<CachedRow> getPrimaryKeys(String catalogName, String schemaName, String table) throws SQLException {
            return cacheResultSet(databaseMetaData.getPrimaryKeys(catalogName, schemaName, table));
        }

        public List<CachedRow> query(String sql) throws SQLException {
            Statement statement = this.getDatabaseMetaData().getConnection().createStatement();
            try {
                return cacheResultSet(statement.executeQuery(sql));
            } finally {
                statement.close();
            }
        }
    }

    private List<CachedRow> cacheResultSet(ResultSet rs) throws SQLException {
        List list = (List) new RowMapperResultSetExtractor(new ColumnMapRowMapper()).extractData(rs);
        for (int i=0; i<list.size(); i++) {
            list.set(i, new CachedRow((Map) list.get(i)));
        }

        return list;
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
            if (o instanceof Short) {
                return ((Short) o).intValue();
            }
            return (Integer) o;
        }

        public Short getShort(String columnName) {
            return (Short) row.get(columnName);
        }

        public Boolean getBoolean(String columnName) {
            return (Boolean) row.get(columnName);
        }
    }

}
