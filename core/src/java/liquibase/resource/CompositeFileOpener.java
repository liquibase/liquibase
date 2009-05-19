package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * A FileOpener that will search in a List of other FileOpeners until it finds
 * one that has a resource of the appropriate name and path.
 *
 * @author <a href="mailto:csuml@yahoo.co.uk>Paul Keeble</a>
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
     * <p/>
     * If none of the FileOpeners was able to produce a stream to the file
     * then null is returned.
     */
    public InputStream getResourceAsStream(String file) throws IOException {
        for (FileOpener o : openers) {
            InputStream is = o.getResourceAsStream(file);
            if (is != null)
                return is;
        }
        return null;
    }

    /**
     * Searches all of the FileOpeners for a directory named packageName. If no
     * results are found within any of the directories then an empty
     * Enumeration is returned.
     */
    public Enumeration<URL> getResources(String packageName) throws IOException {
        for (FileOpener o : openers) {
            Enumeration<URL> e = o.getResources(packageName);
            if (e.hasMoreElements())
                return e;
        }
        return new Vector<URL>().elements();
    }

    public ClassLoader toClassLoader() {
        List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
        for (FileOpener fo: openers) {
            classLoaders.add(fo.toClassLoader());
        }

        return new CompositeClassLoader(classLoaders.toArray(new ClassLoader[classLoaders.size()]));
    }

    //based on code from http://fisheye.codehaus.org/browse/xstream/trunk/xstream/src/java/com/thoughtworks/xstream/core/util/CompositeClassLoader.java?r=root
    private static class CompositeClassLoader extends ClassLoader {

        private final List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();

        public CompositeClassLoader(ClassLoader... classLoaders) {
            this.classLoaders.addAll(Arrays.asList(classLoaders));
        }

        @Override
        public Class loadClass(String name) throws ClassNotFoundException {
            for (Object classLoader1 : classLoaders) {
                ClassLoader classLoader = (ClassLoader) classLoader1;
                try {
                    return classLoader.loadClass(name);
                } catch (ClassNotFoundException notFound) {
                    // ok.. try another one
                }
            }

            // One last try - the context class loader associated with the current thread. Often used in j2ee servers.
            // Note: The contextClassLoader cannot be added to the classLoaders list up front as the thread that constructs
            // liquibase is potentially different to thread that uses it.
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                return contextClassLoader.loadClass(name);
            } else {
                throw new ClassNotFoundException(name);
            }


        }

 	}
}
