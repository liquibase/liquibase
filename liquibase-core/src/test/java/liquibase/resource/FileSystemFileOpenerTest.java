package liquibase.resource;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;


public class FileSystemFileOpenerTest {
    FileSystemResourceAccessor opener;
    
    @Before
    public void createFileOpener() throws URISyntaxException {
        File thisClassFile = new File(new URI(this.getClass().getClassLoader().getResource("liquibase/resource/FileSystemResourceAccessor.class").toExternalForm()));
        String packageDirectory = thisClassFile.getParent();

        
        opener = new FileSystemResourceAccessor(packageDirectory);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void onlyAcceptsDirectories() throws URISyntaxException {
        File thisClassFile = new File(new URI(this.getClass().getClassLoader().getResource("liquibase/resource/FileSystemResourceAccessor.class").toExternalForm()));

        ResourceAccessor o = new FileSystemResourceAccessor(thisClassFile.getAbsolutePath());
    }

    @Test
    public void singleFileTest() throws IOException {
        assertNotNull(opener.getResourceAsStream("FileSystemResourceAccessor.class"));
    }
    
    @Test
    public void multipleFileTest() throws IOException {
        Enumeration<URL> files = opener.getResources(".");
        boolean found = false;
        while(files.hasMoreElements()) {
            URL u = files.nextElement();
            found |=u.getFile().lastIndexOf("FileSystemResourceAccessor")>-1;
        }
        assertTrue(found);
    }
    
    @Test
    public void ahphabeticalOrderTest() throws IOException {
    	Enumeration<URL> files = opener.getResources(".");
    	boolean correct = false;
    	String lastFile = null;
        while(files.hasMoreElements()) {
            URL u = files.nextElement();
            String currentFile = u.getFile().substring(u.getFile().lastIndexOf("/") + 1);
            if (lastFile != null) {
            	correct |= lastFile.compareTo(currentFile) < 0;
            }
            lastFile = currentFile;
        }
        
    	assertTrue(correct);
    }
}
