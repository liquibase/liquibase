package liquibase.resource;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.util.StringUtils;
import liquibase.util.SpringBootFatJar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
        path = convertToPath(relativeTo, path);

        Enumeration<URL> fileUrls = classLoader.getResources(path);

        Set<String> returnSet = new HashSet<>();

        if (!fileUrls.hasMoreElements() && (path.startsWith("jar:") || path.startsWith("file:") || path.startsWith("wsjar:file:") || path.startsWith("zip:"))) {
            fileUrls = new Vector<>(Arrays.asList(new URL(path))).elements();
        }

        while (fileUrls.hasMoreElements()) {
            URL fileUrl = fileUrls.nextElement();

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

                path = SpringBootFatJar.getPathForResource(path);
                if (path.startsWith("classpath:")) {
                    path = path.replaceFirst("classpath:", "");
                }
                if (path.startsWith("classpath*:")) {
                    path = path.replaceFirst("classpath\\*:", "");
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

                        if (entry.getName().startsWith(path)) {

                            if (!recursive) {
                                String pathAsDir = path.endsWith("/")
                                        ? path
                                        : path + "/";
                                if (!entry.getName().startsWith(pathAsDir)
                                 || entry.getName().substring(pathAsDir.length()).contains("/")) {
                                    continue;
                                }
                            }

                            if (entry.isDirectory() && includeDirectories) {
                                returnSet.add(entry.getName());
                            } else if (includeFiles) {
                                returnSet.add(entry.getName());
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
                        getContents(file, recursive, includeFiles, includeDirectories, path, returnSet);
                    }
                } catch (URISyntaxException | IllegalArgumentException e) {
                    //not a local file
                }
            }

            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                String url = resources.nextElement().toExternalForm();
                url = url.replaceFirst("^\\Q" + path + "\\E", "");
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
            description = StringUtils.join(urls, ",");
        } else {
            description = classLoader.getClass().getName();
        }
        return getClass().getName()+"("+ description +")";

    }
}
