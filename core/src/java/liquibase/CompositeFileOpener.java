package liquibase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * A FileOpener that will search in a List of other FileOpeners until it finds
 * one that has a resource of the appropriate name and path.
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk>Paul Keeble</a>
 *
 */
public class CompositeFileOpener implements FileOpener {
    List<FileOpener> openers;
    
    /**
     * Creates a Composite Opener with the list specified. The List will
     * be searched in order from beginning to end.
     * 
     * @param openers The list of Openers to use
     */
    public CompositeFileOpener(List<FileOpener> openers) {
        this.openers = openers;
    }
    
    /**
     * Creates a CompositeFileOpener with 2 entries.
     *
     * @param openers The list of Openers to use
     */
    public CompositeFileOpener(FileOpener... openers) {
        this.openers = Arrays.asList(openers);
    }
    
    /**
     * Searches through all of the FileOpeners in order for the file.
     * 
     * If none of the FileOpeners was able to produce a stream to the file
     * then null is returned.
     */
    public InputStream getResourceAsStream(String file) throws IOException {
        for(FileOpener o : openers) {
            InputStream is = o.getResourceAsStream(file);
            if(is!=null)
                return is;
        }
        return null;
    }

    /**
     * Searches all of the FileOpeners for a directory named packageName. If no
     * results are found within any of the directories then an empty
     * Enumeration is returned.
     * 
     */
    public Enumeration<URL> getResources(String packageName) throws IOException {
        for(FileOpener o : openers) {
            Enumeration<URL> e = o.getResources(packageName);
            if(e.hasMoreElements())
                return e;
        }
        return new Vector<URL>().elements();
    }

}
