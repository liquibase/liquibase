package liquibase.snapshot;

import liquibase.database.Database;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResultSetCacheTest {

    /**
     * Regression test for <a href="https://github.com/liquibase/liquibase/issues/7780">#7780</a>:
     * {@link ResultSetCache.ResultSetExtractor#extract(ResultSet)} is frequently fed a {@link ResultSet}
     * obtained directly from a {@link java.sql.DatabaseMetaData} call (e.g. {@code getColumns}/{@code getTables}),
     * so the caller keeps no reference to the producing {@link Statement}. Closing only the ResultSet
     * leaks that Statement until the connection is closed. extract() must close the owning Statement too.
     */
    @Test
    public void extractClosesOwningStatement() throws Exception {
        Database database = mock(Database.class);

        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getStatement()).thenReturn(statement);
        when(resultSet.next()).thenReturn(false);

        ResultSetCache.ResultSetExtractor extractor = new ResultSetCache.ResultSetExtractor(database) {
            @Override
            public boolean bulkContainsSchema(String schemaKey) {
                return false;
            }

            @Override
            public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                return null;
            }

            @Override
            public ResultSetCache.RowData wantedKeyParameters() {
                return null;
            }

            @Override
            public List<CachedRow> fastFetch() {
                return null;
            }

            @Override
            public List<CachedRow> bulkFetch() {
                return null;
            }
        };

        extractor.extract(resultSet);

        verify(resultSet).close();
        verify(statement).close();
    }
}
