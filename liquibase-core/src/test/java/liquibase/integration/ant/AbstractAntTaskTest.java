package liquibase.integration.ant;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public abstract class AbstractAntTaskTest {
    protected static void setProperties() {
        // Main source root
        String name = DatabaseUpdateTask.class.getName();
        final String resourceName = "/" + name.replace('.', '/') + ".class";
        String absoluteFilePath = DatabaseUpdateTask.class.getResource(resourceName).getFile();
        try {
            absoluteFilePath = URLDecoder.decode(absoluteFilePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Missing UTF-8 encoding in JVM.", e);
        }
        String classesDir = absoluteFilePath.substring(0, absoluteFilePath.length() - resourceName.length());
        System.setProperty("liquibase.test.classes.root", classesDir);

        // Test source root
        String testClassName = DatabaseUpdateTaskTest.class.getName();
        String testSimpleName = DatabaseUpdateTaskTest.class.getSimpleName() + ".class";
        final String testResourceName = "/" + testClassName.replace('.', '/') + ".class";
        String testAbsoluteFilePath = DatabaseUpdateTaskTest.class.getResource(testResourceName).getFile();
        try {
            testAbsoluteFilePath = URLDecoder.decode(testAbsoluteFilePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Missing UTF-8 encoding in JVM.", e);
        }
        String testClassesDir = testAbsoluteFilePath.substring(0, testAbsoluteFilePath.length() - testResourceName.length());
        String testAntBaseDir = testAbsoluteFilePath.substring(0, testAbsoluteFilePath.length() - testSimpleName.length());
        System.setProperty("liquibase.test.testclasses.root", testClassesDir);
        System.setProperty("liquibase.test.ant.basedir", testAntBaseDir);
    }
}
