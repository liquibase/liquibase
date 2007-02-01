package liquibase.migrator;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public abstract class AbstractFileOpenerTest extends TestCase {

    private FileOpener fileOpener;

    protected abstract FileOpener createFileOpener();

    protected void setUp() throws Exception {
        super.setUp();
        fileOpener = createFileOpener();
    }

    public void testGetResourceAsStream() throws Exception {
        try {
            fileOpener.getResourceAsStream("non/existant/file.txt");
            fail("Exception should have been thrown");
        } catch (IOException e) {
            ; //what we wanted
        }
        InputStream inputStream = fileOpener.getResourceAsStream("liquibase/migrator/ant/AntFileOpenerTest.class");
        assertNotNull(inputStream);
    }


    public void testGetResources() throws Exception {
        Enumeration<URL> resources = fileOpener.getResources("liquibase/migrator/ant");
        assertNotNull(resources);
    }


}
