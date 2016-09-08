package liquibase.integration.ant;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.TestSuite;
import org.apache.ant.antunit.junit3.AntUnitSuite;


public abstract class AbstractAntTaskTest {
    // static until upgrade to ant 1.9.x
    protected static TestSuite buildSuite(String testFile,Class<?> testClass) throws URISyntaxException {
        System.setProperty("liquibase.test.ant.basedir", "src/test/resources/liquibase/integration/ant/");
        URL resource = ChangeLogSyncTaskTest.class.getResource("/liquibase/integration/ant/"+testFile);
        File file = new File(resource.toURI());
        return new AntUnitSuite(file, testClass);
    }
}
