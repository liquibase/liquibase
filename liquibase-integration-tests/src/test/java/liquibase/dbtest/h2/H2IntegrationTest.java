package liquibase.dbtest.h2;

import liquibase.dbtest.AbstractIntegrationTest;
import org.junit.Test;

import java.util.Locale;

public class H2IntegrationTest extends AbstractIntegrationTest {

    public H2IntegrationTest() throws Exception {
        super("h2", "jdbc:h2:mem:liquibase");
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
