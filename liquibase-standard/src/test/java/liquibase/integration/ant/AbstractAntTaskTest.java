package liquibase.integration.ant;

import liquibase.Scope;
import liquibase.TestScopeManager;
import liquibase.GlobalConfiguration;
import org.junit.BeforeClass;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public abstract class AbstractAntTaskTest {

    @BeforeClass
    public static void setup() throws Exception {
        Scope.setScopeManager(new TestScopeManager(Scope.getCurrentScope()));
    }

    protected static void setProperties() {
        // Main source root
        String name = BaseLiquibaseTask.class.getName();
        final String resourceName = "/" + name.replace('.', '/') + ".class";
        String absoluteFilePath = BaseLiquibaseTask.class.getResource(resourceName).getFile();
        try {
            absoluteFilePath = URLDecoder.decode(absoluteFilePath, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Missing UTF-8 encoding in JVM.", e);
        }
        String classesDir = absoluteFilePath.substring(0, absoluteFilePath.length() - resourceName.length());
        System.setProperty("liquibase.test.classes.root", classesDir);

        // Test source root
        String testClassName = AbstractAntTaskTest.class.getName();
        String testSimpleName = AbstractAntTaskTest.class.getSimpleName() + ".class";
        final String testResourceName = "/" + testClassName.replace('.', '/') + ".class";
        String testAbsoluteFilePath = AbstractAntTaskTest.class.getResource(testResourceName).getFile();
        try {
            testAbsoluteFilePath = URLDecoder.decode(testAbsoluteFilePath, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Missing UTF-8 encoding in JVM.", e);
        }
        String testClassesDir = testAbsoluteFilePath.substring(0, testAbsoluteFilePath.length() - testResourceName.length());
        String testAntBaseDir = testAbsoluteFilePath.substring(0, testAbsoluteFilePath.length() - testSimpleName.length());
        System.setProperty("liquibase.test.testclasses.root", testClassesDir);
        System.setProperty("liquibase.test.ant.basedir", testAntBaseDir);
    }
}
