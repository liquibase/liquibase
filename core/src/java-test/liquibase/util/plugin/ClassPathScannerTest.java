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
    private CompositeResourceAccessor resourceAccessor;
    private ClassPathScanner classPathScanner;

    @Before
    public void setup() throws Exception{
        resourceAccessor = new CompositeResourceAccessor(new ClassLoaderResourceAccessor(), new ClassLoaderResourceAccessor(new URLClassLoader(new URL[] {
                new File(TestContext.getInstance().findProjectRoot(), "/lib-test/liquibase-samples.jar").toURL()
        })));;
        
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
        Class[] classes = classPathScanner.getClasses("liquibase.parser", ChangeLogParser.class);
        assertTrue(classes.length > 0);
    }


    @Test
    public void getClasses_sampleJar() throws Exception {
        Class[] classes = ClassPathScanner.getInstance().getClasses("liquibase.sqlgenerator", SqlGenerator.class);
        for (Class clazz : classes) {
            if (clazz.getName().equals("liquibase.sqlgenerator.ext.sample1.Sample1UpdateGenerator")) {
                return;
            }
            System.out.println(clazz.getName());
        }
        fail("Did not find Sample1UpdateGenerator");
    }
}
