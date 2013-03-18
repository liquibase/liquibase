package liquibase.dbtest.h2;

import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.diff.output.report.DiffToReport;
import liquibase.snapshot.*;
import org.junit.Test;

public class H2IntegrationTest extends AbstractIntegrationTest {

    private final String dbmsExcludeChangelog;

    public H2IntegrationTest() throws Exception {
        super("h2", "jdbc:h2:mem:liquibase");

        this.dbmsExcludeChangelog = "changelogs/h2/complete/dbms.exclude.changelog.xml";
    }

    @Test
    public void diffToPrintStream() throws Exception{
        if (getDatabase() == null) {
            return;
        }

        runCompleteChangeLog();

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(getDatabase(), null, new CompareControl());
        new DiffToReport(diffResult, System.out).print();
    }

    @Test
    public void diffToChangeLog() throws Exception{
        if (getDatabase() == null) {
            return;
        }

        runCompleteChangeLog();

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(getDatabase(), null, new CompareControl());
        new DiffToChangeLog(diffResult, new DiffOutputControl(true, true, true)).print(System.out);
    }

    @Test
    public void snapshot() throws Exception {
        if (getDatabase() == null) {
            return;
        }


        runCompleteChangeLog();
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(getDatabase().getDefaultSchema(), getDatabase(), new SnapshotControl());
        System.out.println(snapshot);
    }

    @Test
    public void h2IsExcludedFromRunningChangeset() throws Exception {
        runChangeLogFile(dbmsExcludeChangelog);
    }

    //    @Test
//    public void testUpdateWithTurkishLocale() throws Exception {
//        Locale originalDefault = Locale.getDefault();
//
//        Locale.setDefault(new Locale("tr","TR"));
//        testRunChangeLog();
//        Locale.setDefault(originalDefault);
//    }

}
