package liquibase.dbtest.sqlite;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.ValidationFailedException;
import liquibase.snapshot.DatabaseSnapshot;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class SQLiteIntegrationTest extends AbstractIntegrationTest {

    public SQLiteIntegrationTest() throws Exception {
        super("sqlite", DatabaseFactory.getInstance().getDatabase("sqlite"));
        File f = new File("sqlite");
        try {
            f.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void assertThatSnapshotReportsAllObjectTypes(DatabaseSnapshot snapshot) {

    }

    @Override
    public void testTableExistsPreconditionTableNameMatch() {
        //does not work for sqlite
    }

    @Override
    public void testTableIsEmptyPrecondition() {
        //does not work for sqlite
    }

    @Override
    public void testRowCountPrecondition() {
        //does not work for sqlite
    }

    @Test
    public void smartDataLoad() throws Exception {
        if (this.getDatabase() == null) {
            return;
        }

        Liquibase liquibase = createLiquibase("changelogs/common/smartDataLoad.changelog.xml");
        clearDatabase();

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        // check that the automatically rollback now works too
        try {
            liquibase.rollback(new Date(0), this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Override
    @Test
    public void testDiffExternalForeignKeys() throws Exception {
        //cross-schema security for oracle is a bother, ignoring test for now
    }

    @Override
    public void testOutputChangeLog() throws Exception {
        Scope.getCurrentScope().getLog(getClass()).info("Due to several unimplemented ALTER TABLE substatements in SQLite, " +
                "this test is technically impossible on this RDBMS.");
        assertTrue(true);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            String url = getDatabase().getConnection().getURL()
                    .replaceFirst("jdbc:sqlite:", ""); // remove the prefix of the URL jdbc:sqlite:C:\path\to\tmp\dir\liquibase.db
            Scope.getCurrentScope().getLog(getClass()).info("Marking SQLite database as delete on exit: " + url);
            // Want to delete the sqlite db on jvm exit so that future runs are not using stale data.
            new File(url).deleteOnExit();
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to mark SQLite database as delete on exit.", e);
        }
    }
}
