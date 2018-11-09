package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.jvm.ColumnMapRowMapper;
import liquibase.executor.jvm.RowMapperResultSetExtractor;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

class ResultSetCache {
    private Map<String, Integer> timesSingleQueried = new HashMap<>();
    private Map<String, Boolean> didBulkQuery = new HashMap<>();

    private Map<String, Map<String, List<CachedRow>>> cacheBySchema = new HashMap<>();

    private Map<String, Object> info = new HashMap<>();

    public List<CachedRow> get(ResultSetExtractor resultSetExtractor) throws DatabaseException {
        try {
            String wantedKey = resultSetExtractor.wantedKeyParameters().createParamsKey(resultSetExtractor.database);

            String schemaKey = resultSetExtractor.wantedKeyParameters().createSchemaKey(resultSetExtractor.database);

            Map<String, List<CachedRow>> cache = cacheBySchema.get(schemaKey);
            if (cache == null) {
                cache = new HashMap<>();
                cacheBySchema.put(schemaKey, cache);
            }

            if (cache.containsKey(wantedKey)) {
                return cache.get(wantedKey);
            }

            if (didBulkQuery.containsKey(schemaKey) && didBulkQuery.get(schemaKey)) {
                return new ArrayList<>();
            }

            List<CachedRow> results;
            boolean bulkQueried = false;
            if (resultSetExtractor.shouldBulkSelect(schemaKey, this)) {

                //remove any existing single fetches that may be duplicated
                if (resultSetExtractor.bulkContainsSchema(schemaKey)) {
                    for (Map cachedValue : cacheBySchema.values()) {
                        cachedValue.clear();
                    }
                } else {
                    cache.clear();
                }

                results = resultSetExtractor.bulkFetch();
                didBulkQuery.put(schemaKey, true);
                bulkQueried = true;
            } else {
                // Don't store results in real cache to prevent confusion if later fetching all items.
                cache = new HashMap<>();
                Integer previousCount = timesSingleQueried.get(schemaKey);
                if (previousCount == null) {
                    previousCount = 0;
                }
                timesSingleQueried.put(schemaKey, previousCount + 1);
                results = resultSetExtractor.fastFetch();
            }

            for (CachedRow row : results) {
                for (String rowKey : resultSetExtractor.rowKeyParameters(row).getKeyPermutations()) {
                    if (bulkQueried && resultSetExtractor.bulkContainsSchema(schemaKey)) {
                        String rowSchema = resultSetExtractor.getSchemaKey(row).toLowerCase();
                        cache = cacheBySchema.get(rowSchema);
                        if (cache == null) {
                            cache = new HashMap<String, List<CachedRow>>();
                            cacheBySchema.put(rowSchema, cache);
                        }
                    }
                    if (!cache.containsKey(rowKey)) {
                        cache.put(rowKey, new ArrayList<CachedRow>());
                    }
                    cache.get(rowKey).add(row);
                }
            }

            if (bulkQueried) {
                cache = cacheBySchema.get(schemaKey);
            }
            List<CachedRow> returnList = cache.get(wantedKey);
            if (returnList == null) {
                returnList = new ArrayList<>();
            }
            return returnList;


        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public <T> T getInfo(String key, Class<T> type) {
        return (T) info.get(key);
    }

    public void putInfo(String key, Object value) {
        info.put(key, value);
    }

    private int getTimesSingleQueried(String schemaKey) {
        Integer integer = timesSingleQueried.get(schemaKey);
        if (integer == null) {
            return 0;
        }
        return integer;
    }

    public static class RowData {
        private Database database;
        private String[] parameters;
        private String catalog;
        private String schema;

        private String[] keyPermutations;

        protected RowData(String catalog, String schema, Database database, String... parameters) {
            this.database = database;
            this.catalog = catalog;
            this.schema = schema;

            this.parameters = parameters;
        }

        public String[] getKeyPermutations() {
            if (keyPermutations == null) {
                this.keyPermutations = permutations(parameters);

            }
            return keyPermutations;
        }

        protected String[] permutations(String[] params) {
            return permute(params, 0);
        }

        private String[] permute(String[] params, int fromIndex) {
            String[] nullVersion = Arrays.copyOf(params, params.length);
            nullVersion[fromIndex] = null;
            if (params.length == (fromIndex + 1)) {
                return new String[]{
                        createKey(database, params),
                        createKey(database, nullVersion)
                };
            } else {
                List<String> permutations = new ArrayList<>();

                Collections.addAll(permutations, permute(params, fromIndex + 1));
                Collections.addAll(permutations, permute(nullVersion, fromIndex + 1));

                return permutations.toArray(new String[permutations.size()]);
            }
        }

        public String createSchemaKey(Database database) {
            if (!database.supportsCatalogs() && !database.supportsSchemas()) {
                return "all";
            } else if (database.supportsCatalogs() && database.supportsSchemas()) {
                return (catalog + "." + schema).toLowerCase();
            } else {
                if ((catalog == null) && (schema != null)) {
                    return schema.toLowerCase();
                } else {
                    if (catalog == null) {
                        return "all";
                    }
                    return catalog.toLowerCase();
                }
            }
        }

        public String createKey(Database database, String... params) {
            String key = StringUtil.join(params, ":");
            if (!database.isCaseSensitive()) {
                return key.toLowerCase();
            }
            return key;
        }

        public String createParamsKey(Database database) {
            return createKey(database, parameters);
        }
    }

//    public abstract static class MultiSchemaRowData extends RowData {
//        public MultiSchemaRowData(String catalog, String schema, Database database, String... parameters) {
//            super(catalog, schema, database, parameters);
//        }
//
//        @Override
//        public String createSchemaKey(Database database) {
//            String multiSchemaKey = getMultiSchemaKey();
//
//            if (multiSchemaKey == null) {
//                return super.createSchemaKey(database);
//            }
//
//            for (Class<? extends Database> supportedDb : getMultiSchemaSupportedDatabases()) {
//                if (supportedDb.isAssignableFrom(database.getClass())) {
//                    return multiSchemaKey;
//                }
//            }
//            return super.createSchemaKey(database);
//        }
//
//        public abstract Class<? extends Database>[] getMultiSchemaSupportedDatabases();
//
//        public abstract String getMultiSchemaKey();
//    }

    public abstract static class ResultSetExtractor {

        private final Database database;

        public ResultSetExtractor(Database database) {
            this.database = database;
        }

        public abstract boolean bulkContainsSchema(String schemaKey);

        public String getSchemaKey(CachedRow row) {
            throw new UnexpectedLiquibaseException("Not Implemented");
        }

        boolean shouldBulkSelect(String schemaKey, ResultSetCache resultSetCache) {
            return resultSetCache.getTimesSingleQueried(schemaKey) >= 3;
        }

        List<CachedRow> executeAndExtract(String sql, Database database) throws DatabaseException, SQLException {
            return executeAndExtract(sql, database, false);
        }

        List<CachedRow> executeAndExtract(String sql, Database database, boolean informixTrimHint)
                throws DatabaseException, SQLException {
            if (sql == null) {
                return new ArrayList<>();
            }
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                JdbcConnection connection = (JdbcConnection) database.getConnection();
                statement = connection.createStatement();
                resultSet = statement.executeQuery(sql);
                resultSet.setFetchSize(database.getFetchSize());
                return extract(resultSet, informixTrimHint);
            } finally {
                JdbcUtils.close(resultSet, statement);
            }
        }

        public boolean equals(Object expectedValue, Object foundValue) {
            return equals(expectedValue, foundValue, true);
        }

        public boolean equals(Object expectedValue, Object foundValue, boolean equalIfEitherNull) {
            if ((expectedValue == null) && (foundValue == null)) {
                return true;
            }
            if ((expectedValue == null) || (foundValue == null)) {
                return equalIfEitherNull;
            }

            return expectedValue.equals(foundValue);
        }


        public abstract RowData rowKeyParameters(CachedRow row);

        public abstract RowData wantedKeyParameters();

        public abstract List<CachedRow> fastFetch() throws SQLException, DatabaseException;

        public abstract List<CachedRow> bulkFetch() throws SQLException, DatabaseException;

        protected List<CachedRow> extract(ResultSet resultSet) throws SQLException {
            return extract(resultSet, false);
        }

        protected List<CachedRow> extract(ResultSet resultSet, final boolean informixIndexTrimHint)
                throws SQLException {
            resultSet.setFetchSize(database.getFetchSize());
            List<Map> result;
            List<CachedRow> returnList = new ArrayList<>();
            try {
                result = (List<Map>) new RowMapperResultSetExtractor(new ColumnMapRowMapper() {
                    @Override
                    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
                        Object value = super.getColumnValue(rs, index);
                        if ((value != null) && (value instanceof String)) {

                            // Don't trim for informix database,
                            // We need to discern the space in front of an index name,
                            // to know if it was auto-generated or not

                            if (informixIndexTrimHint == false) {
                                value = ((String) value).trim(); // Trim the value normally
                            } else {
                                boolean startsWithSpace = false;
                                if ((database instanceof InformixDatabase) && ((String) value).matches("^ .*$")) {
                                    startsWithSpace = true; // Set the flag if the value started with a space
                                }
                                value = ((String) value).trim(); // Trim the value normally
                                if (startsWithSpace == true) {
                                    value = " " + value; // Put the space back at the beginning if the flag was set
                                }
                            }

                        }
                        return value;
                    }
                }).extractData(resultSet);

                for (Map row : result) {
                    returnList.add(new CachedRow(row));
                }
            } finally {
                JdbcUtils.closeResultSet(resultSet);
            }
            return returnList;
        }
    }

    public abstract static class SingleResultSetExtractor extends ResultSetExtractor {

        public SingleResultSetExtractor(Database database) {
            super(database);
        }

        public abstract List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException;

        public abstract List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException;

        @Override
        public List<CachedRow> fastFetch() throws SQLException, DatabaseException {
            return fastFetchQuery();
        }


        @Override
        public List<CachedRow> bulkFetch() throws SQLException, DatabaseException {
            return bulkFetchQuery();
        }
    }

    public abstract static class UnionResultSetExtractor extends ResultSetExtractor {
        protected UnionResultSetExtractor(Database database) {
            super(database);
        }
    }
}
