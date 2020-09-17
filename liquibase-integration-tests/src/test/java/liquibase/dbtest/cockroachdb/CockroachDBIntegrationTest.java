package liquibase.dbtest.cockroachdb;

import liquibase.CatalogAndSchema;
import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.junit.Test;

import java.sql.SQLSyntaxErrorException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class CockroachDBIntegrationTest extends AbstractIntegrationTest {

    public CockroachDBIntegrationTest() throws Exception {
        super("cockroachdb", DatabaseFactory.getInstance().getDatabase("cockroachdb"));
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        return true;
    }

    @Test
    public void snapshot() throws Exception {
        if (getDatabase() == null) {
            return;
        }

        runCompleteChangeLog();
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(getDatabase().getDefaultSchema(), getDatabase(), new SnapshotControl(getDatabase()));
        System.out.println(snapshot);
    }

    @Test
    public void descPrimaryKey() throws Exception {
        if (getDatabase() == null) {
            return;
        }
        ExecutorService.getInstance().getExecutor(getDatabase()).execute(new RawSqlStatement("DROP TABLE IF EXISTS pk"));

        ExecutorService.getInstance().getExecutor(getDatabase()).execute(new RawSqlStatement("CREATE TABLE pk (\n" +
                "a INT8 NOT NULL,\n" +
                "b INT8 NOT NULL,\n" +
                "c INT8 NOT NULL,\n" +
                "d INT8 NOT NULL,\n" +
                "CONSTRAINT \"primary\" PRIMARY KEY (a ASC, b ASC, c DESC)\n" +
                ")"));

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(getDatabase().getDefaultSchema(), getDatabase(), new SnapshotControl(getDatabase()));
        PrimaryKey pk = snapshot.get(new PrimaryKey().setTable(new Table().setName("pk")).setName("primary"));

        List<Column> columns = pk.getColumns();
        assertEquals("a", columns.get(0).getName());
        assertNull(columns.get(0).getDescending());
        assertEquals("b", columns.get(1).getName());
        assertNull(columns.get(1).getDescending());
        assertEquals("c", columns.get(2).getName());
        assertTrue(columns.get(2).getDescending());
    }
}
