package liquibase.resource;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.util.CollectionUtil;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Manifest;

/**
 * An implementation of {@link FileSystemResourceAccessor} that builds up the file roots based on the passed {@link ClassLoader}.
 * If you are using a ClassLoader that isn't based on local files, you will need to use a different {@link ResourceAccessor} implementation.
 *
 * @see OSGiResourceAccessor for OSGi-based classloaders
 */
public class ClassLoaderResourceAccessor extends FileSystemResourceAccessor {

    public ClassLoaderResourceAccessor(ClassLoader... classLoaders) {
        try {
            for (ClassLoader classLoader : CollectionUtil.createIfNull(classLoaders)) {
                for (Path path : findRootPaths(classLoader)) {
                    addRootPath(path);
                }
                Enumeration<URL> manifests = classLoader.getResources("META-INF/MANIFEST.MF");
                while (manifests.hasMoreElements()) {
                    URL manifestUrl = manifests.nextElement();

                    File jarFile = new File(manifestUrl.getFile()
                            .replaceFirst("^jar:", "")
                            .replaceFirst("^file:/", "")
                            .replaceFirst("!.*", ""));


                    Manifest manifest = new Manifest(manifestUrl.openStream());
                    String classpath = manifest.getMainAttributes().getValue("Class-Path");
                    if (classpath != null) {
                        for (String entry : classpath.split("\\s+")) {
                            entry = entry.replaceFirst("file:", "");
                            File entryFile = new File(entry);

                            if (entryFile.exists()) {
                                addRootPath(entryFile.toPath());
                            } else {
                                Scope.getCurrentScope().getLog(getClass()).info("Missing file " + entryFile.getAbsolutePath() + " referenced in " + jarFile.getAbsolutePath() + "!/META-INF/MANIFEST.MF");
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Called by constructor to create all the root paths from the given classloader.
     * Works best if the passed classLoader is a {@link URLClassLoader} because it can get the root file/dirs directly.
     * If it is not a URLClassLoader, it still attempts to find files by looking up base packages and MANIFEST.MF files.
     * This may miss some roots, however, and so if you are not using a URLClassLoader consider using a custom subclass.
     */
    protected List<Path> findRootPaths(ClassLoader classLoader) throws Exception {
        if (classLoader == null) {
            return new ArrayList<>();

        }

        Logger logger = Scope.getCurrentScope().getLog(getClass());

        List<URL> returnUrls = new ArrayList<>();
        if (classLoader instanceof URLClassLoader) {
            returnUrls.addAll(CollectionUtil.createIfNull(Arrays.asList(((URLClassLoader) classLoader).getURLs())));
        } else {
            logger.info("Found non-URL ClassLoader classloader " + classLoader.toString() + ". Liquibase will try to figure out now to load resources from it, some may be missed. Consider using a custom ClassLoaderResourceAccessor subclass.");
            Set<String> seenUrls = new HashSet<>(); //need to track externalForm as a string to avoid java problem with URL.equals making a network call

            Enumeration<URL> emptyPathUrls = classLoader.getResources("");
            while (emptyPathUrls.hasMoreElements()) {
                URL url = emptyPathUrls.nextElement();
                if (seenUrls.add(url.toExternalForm())) {
                    returnUrls.add(url);
                }
            }

            Enumeration<URL> metaInfoPathUrls = classLoader.getResources("META-INF");
            while (metaInfoPathUrls.hasMoreElements()) {
                String originalUrl = metaInfoPathUrls.nextElement().toExternalForm();
                String finalUrl = originalUrl.replaceFirst("/META-INF", "");
                if (finalUrl.startsWith("jar:")) {
                    if (finalUrl.endsWith("!")) {
                        finalUrl = finalUrl.replaceFirst("^jar:", "").replaceFirst("!$", "");
                    } else {
                        logger.warning("ClassLoader URL " + finalUrl + " starts with jar: but does not end with !, don't knnow how to handle it. Skipping");
                        continue;
                    }
                }

                URL finalUrlObj = new URL(finalUrl);
                if (seenUrls.add(finalUrlObj.toExternalForm())) {
                    returnUrls.add(finalUrlObj);
                }
            }

        }

        List<Path> returnPaths = new ArrayList<>();
        for (URL url : returnUrls) {
            Path path = rootUrlToPath(url);
            if (path != null) {
                logger.fine("Found classloader root at " + path.toString());
                returnPaths.add(path);
            }
        }
        return returnPaths;
    }

    /**
     * Converts the given URL to a Path. Used by {@link #findRootPaths(ClassLoader)} to convert classloader URLs to Paths to pass to {@link #addRootPath(Path)}.
     *
     * @return null if url cannot or should not be converted to a Path.
     */
    protected Path rootUrlToPath(URL url) {
        String protocol = url.getProtocol();
        switch (protocol) {
            case "file":
                return FileSystems.getDefault().getPath(url.getFile().replace("%20", " ").replaceFirst("/(\\w)\\:/", "$1:/"));
            default:
                Scope.getCurrentScope().getLog(getClass()).warning("Unknown protocol '" + protocol + "' for ClassLoaderResourceAccessor on " + url.toExternalForm());
                return null;
        }
    }
}
