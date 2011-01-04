package liquibase.dbtest.informix;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.diff.Diff;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.util.RegexMatcher;
import liquibase.util.StreamUtil;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class InformixIntegrationTest extends AbstractIntegrationTest {

    public InformixIntegrationTest() throws Exception {
        super("informix", "jdbc:informix-sqli://" + getDatabaseServerHostname("Informix") + ":9088/liquibase:informixserver=ol_ids_1150_1");
    }

    @Test
    @Override
    public void testEncondingUpdatingDatabase() throws Exception {
        /*
           * Informix handles schemas differently
           * It is allowed to have several schemas, but all the tables
           * have to have unique names, even though they are in
           * different schemas.
           */

        Database database = getDatabase();
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase("changelogs/informix/encoding.utf8.changelog.xml");
        liquibase.update(this.contexts);
        DatabaseSnapshot utf8Snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);

        clearDatabase(liquibase);

        liquibase = createLiquibase("changelogs/informix/encoding.latin1.changelog.xml");
        liquibase.update(this.contexts);
        DatabaseSnapshot iso88951Snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);

        //TODO: We need better data diff support to be able to do that
        //Diff diff = new Diff(utf8Snapshot,iso88951Snapshot);
        //diff.setDiffData(true);
        //assertFalse("There are no differences setting the same data in utf-8 and iso-8895-1 "
        //        ,diff.compare().differencesFound());

        //For now we do an approach reading diff data
        Diff[] diffs = new Diff[2];
        diffs[0] = new Diff(utf8Snapshot, iso88951Snapshot);
        diffs[0].setDiffData(true);
        diffs[1] = new Diff(iso88951Snapshot, utf8Snapshot);
        diffs[1].setDiffData(true);
        for (Diff diff : diffs) {
            File tempFile = File.createTempFile("liquibase-test", ".xml");
            tempFile.deleteOnExit();
            FileOutputStream output = new FileOutputStream(tempFile);
            diff.compare().printChangeLog(new PrintStream(output, false, "UTF-8"), database);
            output.close();
            String diffOutput = StreamUtil.getStreamContents(new FileInputStream(tempFile), "UTF-8");
            assertTrue("Update to SQL preserves encoding",
                    new RegexMatcher(diffOutput, new String[]{
                            //For the UTF-8 encoded cvs
                            "value=\"‡ËÏÚ˘·ÈÌÛ˙¿»Ã“Ÿ¡…Õ”⁄‚ÍÓÙ˚‰ÎÔˆ¸\"",
                            "value=\"ÁÒÆ\""
                    }).allMatchedInSequentialOrder());
        }

    }


    @Test
    @Override
    public void testRerunDiffChangeLogAltSchema() throws Exception {
        /*
           * Informix handles schemas differently
           * It is allowed to have several schemas, but all the tables
           * have to have unique names, even though they are in
           * different schemas.
           * So this test is disabled for Informix.
           */

    }

}
