package liquibase.resource;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.util.StringUtil;
import liquibase.util.SpringBootFatJar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * An implementation of {@link liquibase.resource.ResourceAccessor} that wraps a class loader.
 */
public class ClassLoaderResourceAccessor extends AbstractResourceAccessor {

    private ClassLoader classLoader;
    public ClassLoaderResourceAccessor() {
        this.classLoader = getClass().getClassLoader();
        init(); //init needs to be called after classloader is set
    }

    public ClassLoaderResourceAccessor(ClassLoader classLoader) {
        this.classLoader = classLoader;
        init(); //init needs to be called after classloader is set
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        Enumeration<URL> resources = classLoader.getResources(path);
        if ((resources == null) || !resources.hasMoreElements()) {
            return null;
        }
        Set<String> seenUrls = new HashSet<>();
        Set<InputStream> returnSet = new HashSet<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (seenUrls.contains(url.toExternalForm())) {
                continue;
            }
            seenUrls.add(url.toExternalForm());
            LogService.getLog(getClass()).debug(LogType.LOG, "Opening "+url.toExternalForm()+" as "+path);

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            InputStream resourceAsStream = connection.getInputStream();
            if (resourceAsStream != null) {
                returnSet.add(resourceAsStream);
            }
        }

        return returnSet;
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        String sanitizePath = convertToPath(relativeTo, path);

        Enumeration<URL> fileUrls = classLoader.getResources(sanitizePath);

        Set<String> returnSet = new HashSet<>();

        if (!fileUrls.hasMoreElements() && (sanitizePath.startsWith("jar:") || sanitizePath.startsWith("file:") || sanitizePath.startsWith("wsjar:file:") || sanitizePath.startsWith("zip:"))) {
            fileUrls = new Vector<>(Arrays.asList(new URL(sanitizePath))).elements();
        }

        // Improve speed by removing duplicate file returned by getResources
        Set<URL> elements = new HashSet<>();
        while (fileUrls.hasMoreElements()) {
            elements.add(fileUrls.nextElement());
        }

        for (URL fileUrl : elements) {
            if (fileUrl.toExternalForm().startsWith("jar:file:")
                    || fileUrl.toExternalForm().startsWith("wsjar:file:")
                    || fileUrl.toExternalForm().startsWith("zip:")) {

                String[] zipAndFile = fileUrl.getFile().split("!");
                String zipFilePath = zipAndFile[0];
                if (zipFilePath.matches("file:\\/[A-Za-z]:\\/.*")) {
                    zipFilePath = zipFilePath.replaceFirst("file:\\/", "");
                } else {
                    zipFilePath = zipFilePath.replaceFirst("file:", "");
                }
                zipFilePath = URLDecoder.decode(zipFilePath, LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding());

                sanitizePath = SpringBootFatJar.getPathForResource(sanitizePath);
                if (sanitizePath.startsWith("classpath:")) {
                    sanitizePath = sanitizePath.replaceFirst("classpath:", "");
                }
                if (sanitizePath.startsWith("classpath*:")) {
                    sanitizePath = sanitizePath.replaceFirst("classpath\\*:", "");
                }
                // if path is like 'jar:<url>!/{entry}', use the last part as resource path
                if (sanitizePath.contains("!/")) {
                    String[] components = sanitizePath.split("!/");
                    if (components.length > 1) {
                        sanitizePath = components[components.length - 1];
                    }
                }

                // TODO:When we update to Java 7+, we can can create a FileSystem from the JAR (zip)
                // file, and then use NIO's directory walking and filtering mechanisms to search through it.
                //
                // As of 2016-02-03, Liquibase is Java 6+ (1.6)

                // java.util.JarFile has a slightly nicer interface than ZipInputStream here and
                // it works for zip files as well as JAR files
                JarFile zipfile = new JarFile(zipFilePath, false);

                try {
                    Enumeration<JarEntry> entries = zipfile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();

                        if (entry.getName().startsWith(sanitizePath)) {

                            if (!recursive) {
                                String pathAsDir = sanitizePath.endsWith("/") ? sanitizePath : sanitizePath + "/";
                                if (!entry.getName().startsWith(pathAsDir)
                                 || entry.getName().substring(pathAsDir.length()).contains("/")) {
                                    continue;
                                }
                            }

                            if ((entry.isDirectory() && includeDirectories) || (!entry.isDirectory() && includeFiles)) {
                                String returnPath = SpringBootFatJar.getSimplePathForResources(entry.getName(), path);
                                // Find changelog inside nested jar
                                if (entry.getName().endsWith(".jar")) {
                                    JarInputStream jarIS = null;
                                    try {
                                        jarIS = new JarInputStream(zipfile.getInputStream(entry));

                                        JarEntry nestedEntry = jarIS.getNextJarEntry();
                                        while (nestedEntry != null) {
                                            if (nestedEntry.getName().startsWith(returnPath)) {
                                                returnSet.add(nestedEntry.getName());
                                            }
                                            nestedEntry = jarIS.getNextJarEntry();
                                        }
                                    } finally {
                                        if (jarIS != null) {
                                            jarIS.close();
                                        }
                                    }
                                } else {
                                    returnSet.add(returnPath);
                                }
                            }
                        }
                    }
                } finally {
                    zipfile.close();
                }
            } else {
                try {
                    File file = new File(fileUrl.toURI());
                    if (file.exists()) {
                        getContents(file, recursive, includeFiles, includeDirectories, sanitizePath, returnSet);
                    }
                } catch (URISyntaxException | IllegalArgumentException e) {
                    //not a local file
                }
            }

            Enumeration<URL> resources = classLoader.getResources(sanitizePath);

            while (resources.hasMoreElements()) {
                String url = resources.nextElement().toExternalForm();
                url = url.replaceFirst("^\\Q" + sanitizePath + "\\E", "");
                returnSet.add(url);
            }
        }

        if (returnSet.isEmpty()) {
            return null;
        }
        return returnSet;
    }

    @Override
    public ClassLoader toClassLoader() {
        return classLoader;
    }

    @Override
    public String toString() {
        String description;
        if (classLoader instanceof URLClassLoader) {
            List<String> urls = new ArrayList<>();
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                urls.add(url.toExternalForm());
            }
            description = StringUtil.join(urls, ",");
        } else {
            description = classLoader.getClass().getName();
        }
        return getClass().getName()+"("+ description +")";

    }
}
