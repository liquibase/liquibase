package liquibase.snapshot;

import java.sql.SQLException;
import java.util.List;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;

public class BigQueryResultSetCache extends ResultSetCache {
    public static class RowData extends ResultSetCache.RowData {
        public RowData(String catalog, String schema, Database database, String... parameters) {
            super(catalog, schema, database, parameters);
        }
    }

    public abstract static class SingleResultSetExtractor extends ResultSetCache.SingleResultSetExtractor {
        public SingleResultSetExtractor(Database database) {
            super(database);
        }

        @Override
        public List<CachedRow> executeAndExtract(String sql, Database database) throws DatabaseException, SQLException {
            return super.executeAndExtract(sql, database);
        }

        @Override
        public List<CachedRow> executeAndExtract(String sql, Database database, boolean trimHint)
                throws DatabaseException, SQLException {
            return super.executeAndExtract(sql, database, trimHint);
        }

    }
}
