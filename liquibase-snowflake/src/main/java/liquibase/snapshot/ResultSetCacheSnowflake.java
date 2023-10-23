package liquibase.snapshot;

import liquibase.database.Database;

public class ResultSetCacheSnowflake extends ResultSetCache {
    public static class RowData extends ResultSetCache.RowData {
        public RowData(String catalog, String schema, Database database, String... parameters) {
            super(catalog, schema, database, parameters);
        }
    }

    public abstract static class SingleResultSetExtractor extends ResultSetCache.SingleResultSetExtractor {
        public SingleResultSetExtractor(Database database) {
            super(database);
        }
    }
}
