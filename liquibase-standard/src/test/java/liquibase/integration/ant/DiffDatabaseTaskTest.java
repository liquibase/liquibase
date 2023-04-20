package liquibase.integration.ant;

import junit.framework.TestSuite;
import org.apache.ant.antunit.junit3.AntUnitSuite;
import org.apache.ant.antunit.junit4.AntUnitSuiteRunner;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@RunWith(AntUnitSuiteRunner.class)
public class DiffDatabaseTaskTest extends AbstractAntTaskTest {
    public static TestSuite suite() throws URISyntaxException {
        setProperties();
        URL resource = DiffDatabaseTaskTest.class.getResource("/liquibase/integration/ant/DiffDatabaseTaskTest.xml");
        File file = new File(resource.toURI());
        return new AntUnitSuite(file, DiffDatabaseTaskTest.class);
    }
}