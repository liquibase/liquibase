package liquibase.resource;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * A FileOpener implementation which finds Files in the
 * File System.
 * <p/>
 * FileSystemFileOpeners can use a BaseDirectory to determine
 * where relative paths should be resolved from.
 *
 * @author <a href="mailto:csuml@yahoo.co.uk>Paul Keeble</a>
 */
public class FileSystemResourceAccessor implements ResourceAccessor {
    String baseDirectory;

    /**
     * Creates using a Base directory of null, all files will be
     * resolved exactly as they are given.
     */
    public FileSystemResourceAccessor() {
        baseDirectory = null;
    }

    /**
     * Creates using  a supplied base directory.
     *
     * @param base The path to use to resolve relative paths
     */
    public FileSystemResourceAccessor(String base) {
        if (new File(base).isFile())
            throw new IllegalArgumentException("base must be a directory");
        baseDirectory = base;
    }

    /**
     * Opens a stream on a file, resolving to the baseDirectory if the
     * file is relative.
     */
    @Override
    public InputStream getResourceAsStream(String file) throws IOException {
        File absoluteFile = new File(file);
        File relativeFile = (baseDirectory == null) ? new File(file) : new File(baseDirectory, file);

        if (absoluteFile.exists() && absoluteFile.isFile() && absoluteFile.isAbsolute()) {
            return new BufferedInputStream(new FileInputStream(absoluteFile));
        } else if (relativeFile.exists() && relativeFile.isFile()) {
            return new BufferedInputStream(new FileInputStream(relativeFile));
        } else {
            return null;

        }
    }

    @Override
    public Enumeration<URL> getResources(String packageName) throws IOException {
        String directoryPath = (new File(packageName).isAbsolute() || baseDirectory == null) ? packageName : baseDirectory + File.separator + packageName;

        File directoryFile = new File(directoryPath);
        if (!directoryFile.exists()) {
            return new Vector<URL>().elements();
        }

        if (!directoryFile.isDirectory()) {
            return new Vector<URL>(Arrays.asList(directoryFile.toURL())).elements();
        }

        File[] files = directoryFile.listFiles();
        
        List<URL> results = new ArrayList<URL>();

        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory())
                    results.add(f.toURI().toURL());
            }
        }

        final Iterator<URL> it = results.iterator();
        return new Enumeration<URL>() {

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public URL nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public ClassLoader toClassLoader() {
        try {
            return new URLClassLoader(new URL[]{new URL("file://" + baseDirectory)});
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        String dir = baseDirectory;
        if (dir == null) {
            dir = new File(".").getAbsolutePath();
        }
        return getClass().getName()+"("+ dir +")";
    }

}
