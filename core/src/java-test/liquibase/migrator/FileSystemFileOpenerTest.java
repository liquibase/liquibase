package liquibase.migrator;

import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;


public class FileSystemFileOpenerTest {
    FileSystemFileOpener opener;
    
    @Before
    public void createFileOpener() throws URISyntaxException {
        File thisClassFile = new File(new URI(this.getClass().getClassLoader().getResource("liquibase/FileSystemFileOpener.class").toExternalForm()));
        String packageDirectory = thisClassFile.getParent();

        
        opener = new FileSystemFileOpener(packageDirectory);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void onlyAcceptsDirectories() throws URISyntaxException {
        File thisClassFile = new File(new URI(this.getClass().getClassLoader().getResource("liquibase/FileSystemFileOpener.class").toExternalForm()));

        FileOpener o = new FileSystemFileOpener(thisClassFile.getAbsolutePath());
    }

    @Test
    public void singleFileTest() throws IOException {
        assertNotNull(opener.getResourceAsStream("FileSystemFileOpener.class"));
    }
    
    @Test
    public void multipleFileTest() throws IOException {
        Enumeration<URL> files = opener.getResources(".");
        boolean found = false;
        while(files.hasMoreElements()) {
            URL u = files.nextElement();
            found |=u.getFile().lastIndexOf("FileSystemFileOpener")>-1;
        }
        assertTrue(found);
    }
}
