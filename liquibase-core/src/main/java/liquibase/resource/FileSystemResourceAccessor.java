package liquibase.resource;

import liquibase.exception.UnexpectedLiquibaseException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * A @{link ResourceAccessor} implementation which finds Files in the File System.
 */
public class FileSystemResourceAccessor extends AbstractResourceAccessor {

    private File baseDirectory;
    private boolean readyForInit;

    /**
     * Creates with no base directory. All files will be resolved exactly as they are given.
     */
    public FileSystemResourceAccessor() {
        baseDirectory = null;
        readyForInit = true;
        init();
    }

    /**
     * Creates with base directory for relative path support.
     */
    public FileSystemResourceAccessor(String base) {
        baseDirectory = new File(base);
        if (!baseDirectory.isDirectory()) {
            throw new IllegalArgumentException(base + " must be a directory");
        }
        readyForInit = true;
        init();
    }

    @Override
    protected void init() {
        if (readyForInit) {
            super.init();
        }
    }

    @Override
    protected void addRootPath(URL path) {
        try {
            File pathAsFile = new File(path.toURI());

            for (File fileSystemRoot : File.listRoots()) {
                if (pathAsFile.equals(fileSystemRoot)) { //don't include root
                    return;
                }
            }
        } catch (URISyntaxException e) {
            //add like normal
        }

        super.addRootPath(path);
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        File absoluteFile = new File(path);
        File relativeFile = (baseDirectory == null) ? new File(path) : new File(baseDirectory, path);

        InputStream fileStream = null;
        if (absoluteFile.isAbsolute()) {
            try {
                fileStream = openStream(absoluteFile);
            } catch (FileNotFoundException e) {
                //will try relative
            }
        }

        if (fileStream == null) {
            try {
                fileStream = openStream(relativeFile);
            } catch (FileNotFoundException e2) {
                return null;
            }
        }


        Set<InputStream> returnSet = new HashSet<>();
        returnSet.add(fileStream);
        return returnSet;
    }

    private InputStream openStream(File file) throws IOException, FileNotFoundException {
        if (file.getName().toLowerCase().endsWith(".gz")) {
            return new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)));
        } else {
            return new BufferedInputStream(new FileInputStream(file));
        }
    }


    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        File finalDir;

        if (relativeTo == null) {
            finalDir = new File(this.baseDirectory, path);
        } else {
            finalDir = new File(this.baseDirectory, relativeTo);
            finalDir = new File(finalDir.getParentFile(), path);
        }

        if (finalDir.exists() && finalDir.isDirectory()) {
            Set<String> returnSet = new HashSet<>();
            getContents(finalDir, recursive, includeFiles, includeDirectories, path, returnSet);

            SortedSet<String> rootPaths = new TreeSet<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int i = -1 * ((Integer) o1.length()).compareTo(o2.length());
                    if (i == 0) {
                        i = o1.compareTo(o2);
                    }
                    return i;
                }
            });

            for (String rootPath : getRootPaths()) {
                if (rootPath.matches("file:/[A-Za-z]:/.*")) {
                    rootPath = rootPath.replaceFirst("file:/", "");
                } else {
                    rootPath = rootPath.replaceFirst("file:", "");
                }
                rootPaths.add(rootPath.replace("\\", "/"));
            }

            Set<String> finalReturnSet = new LinkedHashSet<>();
            for (String returnPath : returnSet) {
                returnPath = returnPath.replace("\\", "/");
                for (String rootPath : rootPaths) {
                    boolean matches = false;
                    if (isCaseSensitive()) {
                        matches = returnPath.startsWith(rootPath);
                    } else {
                        matches = returnPath.toLowerCase().startsWith(rootPath.toLowerCase());
                    }
                    if (matches) {
                        returnPath = returnPath.substring(rootPath.length());
                        break;
                    }
                }
                finalReturnSet.add(returnPath);
            }
            return finalReturnSet;
        }

        return null;
    }

    @Override
    protected String convertToPath(String string) {
        if (this.baseDirectory == null) {
            return string;
        } else {
            try {
                return "file:" + new File(string).getCanonicalPath().substring(this.baseDirectory.getCanonicalPath().length());
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

    }

    @Override
    public ClassLoader toClassLoader() {
        try {
            URL url;
            if (baseDirectory == null) {
                url = new File("/").toURI().toURL();
            } else {
                url = baseDirectory.toURI().toURL();
            }
            return new URLClassLoader(new URL[]{url});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        File dir = baseDirectory;
        if (dir == null) {
            dir = new File(".");
        }
        return getClass().getName() + "(" + dir.getAbsolutePath() + ")";
    }

}
