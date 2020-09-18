package liquibase.dbtest.pgsql;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.RawSqlStatement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PostgreSQLIntegrationTest extends AbstractIntegrationTest {

    public PostgreSQLIntegrationTest() throws Exception {
        super("pgsql", DatabaseFactory.getInstance().getDatabase("postgresql"));
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        return true;
    }

    @Test
    public void testMissingDataGenerator() throws Exception {
        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("CREATE TABLE \"FIRST_TABLE\" (\"ID\" INT, \"NAME\" VARCHAR(20), \"LAST_NAME\" VARCHAR(20) DEFAULT 'Snow', " +
                                                    "\"AGE\" INT DEFAULT 25, \"REGISTRATION_DATE\" date DEFAULT TO_DATE('2014-08-11', 'YYYY-MM-DD'), " +
                                                    "\"COMPVALCOL\" INT DEFAULT 1*22)"));

        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("CREATE TABLE \"SECOND_TABLE\" (\"ID\" INT, \"NAME\" VARCHAR(20))"));

        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("ALTER TABLE \"FIRST_TABLE\" ADD CONSTRAINT \"FIRST_TABLE_PK\" PRIMARY KEY (\"ID\")"));

        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("ALTER TABLE \"SECOND_TABLE\" ADD CONSTRAINT \"FIRST_TABLE_FK\" FOREIGN KEY (\"ID\") REFERENCES \"FIRST_TABLE\"(\"ID\")"));

        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("CREATE INDEX \"IDX_FIRST_TABLE\" ON \"FIRST_TABLE\"(\"NAME\")"));

        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("INSERT INTO \"FIRST_TABLE\"(\"ID\", \"NAME\") VALUES (1, 'JOHN')"));
        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("INSERT INTO \"FIRST_TABLE\"(\"ID\", \"NAME\", \"LAST_NAME\", \"AGE\", \"REGISTRATION_DATE\", \"COMPVALCOL\") VALUES (2, 'JEREMY', 'IRONS', 71, TO_DATE('2020-04-01', 'YYYY-MM-DD'), 2*11 )"));
        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("INSERT INTO \"SECOND_TABLE\"(\"ID\", \"NAME\") VALUES (1, 'JOHN')"));
        ExecutorService.getInstance().getExecutor(getDatabase())
                  .execute(
                          new RawSqlStatement("INSERT INTO \"SECOND_TABLE\"(\"ID\", \"NAME\") VALUES (2, 'JEREMY')"));
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(getDatabase(), null, new CompareControl());

        DiffToChangeLog changeLogWriter =
                new DiffToChangeLog(diffResult,
                   new DiffOutputControl(false, false, false, null));
        List<ChangeSet> changeSets = changeLogWriter.generateChangeSets();
        boolean found = false;
        for (ChangeSet changeSet : changeSets) {
            List<Change> changes = changeSet.getChanges();
            for (Change change : changes) {
                if (! (change instanceof CreateTableChange)) {
                    continue;
                }
                found = ((CreateTableChange) change).getTableName().equals("FIRST_TABLE");
                if (found) {
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        Assert.assertTrue("There should be a table named \"FIRST_TABLE\"", found);
    }
}
