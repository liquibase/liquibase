package liquibase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * A FileOpener implementation which finds Files in the
 * File System.
 * 
 * FileSystemFileOpeners can use a BaseDirectory to determine
 * where relative paths should be resolved from.
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk>Paul Keeble</a>
 *
 */
public class FileSystemFileOpener implements FileOpener {
    String baseDirectory;
    
    /**
     * Creates using a Base directory of null, all files will be
     * resolved exactly as they are given.
     */
    public FileSystemFileOpener() {
        baseDirectory = null;
    }
    
    /**
     * Creates using  a supplied base directory.
     * 
     * @param base The path to use to resolve relative paths
     */
    public FileSystemFileOpener(String base) {
        if(new File(base).isFile())
            throw new IllegalArgumentException("base must be a directory");
        baseDirectory = base;
    }
    
    /**
     * Opens a stream on a file, resolving to the baseDirectory if the
     * file is relative.
     */
    public InputStream getResourceAsStream(String file) throws IOException {
        File absoluteFile = new File(file);
        File relativeFile = (baseDirectory == null) ? new File(file) : new File(baseDirectory,file);
        
        if(absoluteFile.exists() && absoluteFile.isFile() && absoluteFile.isAbsolute()) {
            return new FileInputStream(absoluteFile);
        } else if (relativeFile.exists() && relativeFile.isFile()){
            return new FileInputStream(relativeFile);
        } else {
            return null;
            
        }
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        String directoryPath = (new File(packageName).isAbsolute() || baseDirectory == null) 
            ? packageName : baseDirectory + File.separator + packageName;
            
        File[] files = new File(directoryPath).listFiles();

        List<URL> results = new ArrayList<URL>();
        
        for(File f : files) {
            results.add(new URL("file://" + f.getCanonicalPath()));
        }
        
        final Iterator<URL> it = results.iterator();
        return new Enumeration<URL>() {

            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public URL nextElement() {
                return it.next();
            }

        };
    }

   
}
