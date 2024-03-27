package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.jvm.ColumnMapRowMapper;
import liquibase.executor.jvm.RowMapperResultSetExtractor;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.JdbcUtil;
import liquibase.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResultSetCache {
    private final Map<String, Integer> timesSingleQueried = new HashMap<>();
    private final Map<String, Boolean> didBulkQuery = new HashMap<>();
    private boolean bulkTracking = true;

    private final Map<String, Map<String, List<CachedRow>>> cacheBySchema = new ConcurrentHashMap<>();

    private final Map<String, Object> info = new ConcurrentHashMap<>();

    public List<CachedRow> get(ResultSetExtractor resultSetExtractor) throws DatabaseException {
        try {
            String wantedKey = resultSetExtractor.wantedKeyParameters().createParamsKey(resultSetExtractor.database);

            String schemaKey = resultSetExtractor.wantedKeyParameters().createSchemaKey(resultSetExtractor.database);

            Map<String, List<CachedRow>> cache = cacheBySchema.computeIfAbsent(schemaKey, k -> new HashMap<>());

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
                didBulkQuery.put(schemaKey, bulkTracking);
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
                        String rowSchema = CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE.
                                equals(resultSetExtractor.database.getSchemaAndCatalogCase())?resultSetExtractor.getSchemaKey(row):
                                resultSetExtractor.getSchemaKey(row).toLowerCase();
                        cache = cacheBySchema.computeIfAbsent(rowSchema, k -> new HashMap<String, List<CachedRow>>());
                    }
                    if (!cache.containsKey(rowKey)) {
                        cache.put(rowKey, new ArrayList<>());
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
        private final Database database;
        private final String[] parameters;
        private final String catalog;
        private final String schema;

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

                return permutations.toArray(new String[0]);
            }
        }

        public String createSchemaKey(Database database) {
            if (!database.supports(Catalog.class) && !database.supports(Schema.class)) {
                return "all";
            } else if (database.supports(Catalog.class) && database.supports(Schema.class)) {
                if (CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE.
                        equals(database.getSchemaAndCatalogCase())) {
                    return (catalog + "." + schema);
                }
                return (catalog + "." + schema).toLowerCase();
            } else {
                if ((catalog == null) && (schema != null)) {
                    if (CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE.
                            equals(database.getSchemaAndCatalogCase())) {
                        return schema;
                    }
                    return schema.toLowerCase();
                } else {
                    if (catalog == null) {
                        return "all";
                    }
                    if (CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE.
                            equals(database.getSchemaAndCatalogCase())) {
                        return catalog;
                    }
                    return catalog.toLowerCase();
                }
            }
        }

        public String createKey(Database database, String... params) {
            String key = StringUtil.join(params, ":");
            if (CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE.
                    equals(database.getSchemaAndCatalogCase())) {
                return key;
            }
            if (!database.isCaseSensitive()) {
                return key.toLowerCase();
            }
            return key;
        }

        public String createParamsKey(Database database) {
            return createKey(database, parameters);
        }
    }

    public abstract static class ResultSetExtractor {

        private final Database database;

        public ResultSetExtractor(Database database) {
            this.database = database;
        }

        public abstract boolean bulkContainsSchema(String schemaKey);

        public String getSchemaKey(CachedRow row) {
            throw new UnexpectedLiquibaseException("Not Implemented");
        }

        protected boolean shouldBulkSelect(String schemaKey, ResultSetCache resultSetCache) {
            return resultSetCache.getTimesSingleQueried(schemaKey) >= 3;
        }

        protected List<CachedRow> executeAndExtract(String sql, Database database) throws DatabaseException, SQLException {
            return executeAndExtract(database, false, sql);
        }

        protected List<CachedRow> executeAndExtract(Database database, String sql, Object...parameters) throws DatabaseException, SQLException {
            return executeAndExtract(database, false, sql, parameters);
        }

        protected List<CachedRow> executeAndExtract(Database database, boolean informixTrimHint, String sql, Object...parameters)
                throws DatabaseException, SQLException {
            if (sql == null) {
                return new ArrayList<>();
            }
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                JdbcConnection connection = (JdbcConnection) database.getConnection();
                statement = connection.prepareStatement(sql);
                for (int i = 0; i < parameters.length; ++i) {
                    statement.setObject(i + 1, parameters[i]);
                }
                resultSet = statement.executeQuery();
                resultSet.setFetchSize(database.getFetchSize());
                return extract(resultSet, informixTrimHint);
            } finally {
                JdbcUtil.close(resultSet, statement);
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
            List<CachedRow> returnList = new ArrayList<>();
            try {
                List<Map> result = (List<Map>) new RowMapperResultSetExtractor(new ColumnMapRowMapper(database.isCaseSensitive()) {
                    @Override
                    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
                        Object value = super.getColumnValue(rs, index);
                        if ((value instanceof String)) {

                            // Don't trim for informix database,
                            // We need to discern the space in front of an index name,
                            // to know if it was auto-generated or not

                            if (informixIndexTrimHint == false) {
                                value = ((String) value).trim(); // Trim the value normally
                            } else {
                                boolean startsWithSpace = (database instanceof InformixDatabase) && ((String) value).matches("^ .*$");
                                // Set the flag if the value started with a space
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
                JdbcUtil.closeResultSet(resultSet);
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

    /**
     * Method to control bulk fetching. By default it is true. Mostly this
     * flag is used when the database supports multi catalog/schema
     * @param bulkTracking - boolean flag to control bulk operation
     */
    public void setBulkTracking(boolean bulkTracking) {
        this.bulkTracking = bulkTracking;
    }
}
