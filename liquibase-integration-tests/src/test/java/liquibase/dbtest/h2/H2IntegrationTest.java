package liquibase.dbtest.h2;

import liquibase.Liquibase;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.diff.DiffControl;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffToPrintStream;
import liquibase.snapshot.*;
import org.junit.Test;

import java.util.Locale;

public class H2IntegrationTest extends AbstractIntegrationTest {

    public H2IntegrationTest() throws Exception {
        super("h2", "jdbc:h2:mem:liquibase");
    }

    @Test
    public void diffToPrintStream() throws Exception{
        if (getDatabase() == null) {
            return;
        }

        runCompleteChangeLog();

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(getDatabase(), null, new DiffControl());
        new DiffToPrintStream(diffResult, System.out).print();
    }

    @Test
    public void snapshot() throws Exception {
        if (getDatabase() == null) {
            return;
        }


        runCompleteChangeLog();
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new SnapshotControl(), getDatabase());
        System.out.println(snapshot);
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
