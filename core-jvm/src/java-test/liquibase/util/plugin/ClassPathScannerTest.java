package liquibase.util.plugin;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import liquibase.parser.ChangeLogParser;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.test.TestContext;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;

public class ClassPathScannerTest {
    private ClassPathScanner classPathScanner;

    @Before
    public void setup() throws Exception{
        File sample1 = new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/lib-test/liquibase-sample1.jar");
        File sample2 = new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/lib-test/liquibase-sample2.jar");
        CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(new ClassLoaderResourceAccessor(), new ClassLoaderResourceAccessor(new URLClassLoader(new URL[]{
                sample1.toURL(),
                sample2.toURL()
        })));
        
        classPathScanner = ClassPathScanner.getInstance();
        classPathScanner.setResourceAccessor(resourceAccessor);
    }

    @After
    public void teardown() {
        ClassPathScanner.reset();
    }
    
    @Test
     public void reset() {
         ClassPathScanner instance1 = ClassPathScanner.getInstance();
         ClassPathScanner.reset();
         assertFalse(instance1 == ClassPathScanner.getInstance());
     }


    @Test
    public void getClasses() throws Exception {
        Class[] classes = classPathScanner.getClasses(ChangeLogParser.class);
        assertTrue(classes.length > 0);
    }


    @Test
    public void getClasses_sampleJar() throws Exception {
        Class[] classes = ClassPathScanner.getInstance().getClasses(SqlGenerator.class);
        for (Class clazz : classes) {
            if (clazz.getName().equals("liquibase.sqlgenerator.ext.sample1.Sample1UpdateGenerator")) {
                return;
            }
        }
        fail("Did not find Sample1UpdateGenerator");
    }
}
