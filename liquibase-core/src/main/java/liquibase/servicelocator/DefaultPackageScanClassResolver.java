package liquibase.servicelocator;

import liquibase.logging.Logger;
import liquibase.logging.core.DefaultLogger;
import liquibase.util.FileUtil;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Default implement of {@link PackageScanClassResolver}
 */
public class DefaultPackageScanClassResolver implements PackageScanClassResolver {

    protected final transient Logger log = new DefaultLogger();
    private Set<ClassLoader> classLoaders;
    private Set<PackageScanFilter> scanFilters;
    private Map<String, Set<Class>> allClassesByPackage = new HashMap<String, Set<Class>>();
    private Set<String> loadedPackages = new HashSet<String>();

    private Map<File, File> unzippedJars = new HashMap<File, File>();

    private Map<String, Set<String>> classFilesByLocation = new HashMap<String, Set<String>>();

    @Override
    public void addClassLoader(ClassLoader classLoader) {
        try {
            getClassLoaders().add(classLoader);
        } catch (UnsupportedOperationException ex) {
            // Ignore this exception as the PackageScanClassResolver
            // don't want use any other classloader
        }
    }

    @Override
    public void addFilter(PackageScanFilter filter) {
        if (scanFilters == null) {
            scanFilters = new LinkedHashSet<PackageScanFilter>();
        }
        scanFilters.add(filter);
    }

    @Override
    public void removeFilter(PackageScanFilter filter) {
        if (scanFilters != null) {
            scanFilters.remove(filter);
        }
    }

    @Override
    public Set<ClassLoader> getClassLoaders() {
        if (classLoaders == null) {
            classLoaders = new HashSet<ClassLoader>();
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                log.debug("The thread context class loader: " + ccl + "  is used to load the class");
                classLoaders.add(ccl);
            }
            classLoaders.add(DefaultPackageScanClassResolver.class.getClassLoader());
        }
        return classLoaders;
    }

    @Override
    public void setClassLoaders(Set<ClassLoader> classLoaders) {
        this.classLoaders = classLoaders;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<?>> findImplementations(Class parent, String... packageNames) {
        if (packageNames == null) {
            return Collections.EMPTY_SET;
        }

        log.debug("Searching for implementations of " + parent.getName() + " in packages: " + Arrays.asList(packageNames));

        PackageScanFilter test = getCompositeFilter(new AssignableToPackageScanFilter(parent));
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        for (String pkg : packageNames) {
            find(test, pkg, classes);
        }

        log.debug("Found: " + classes);

        return classes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<?>> findByFilter(PackageScanFilter filter, String... packageNames) {
        if (packageNames == null) {
            return Collections.EMPTY_SET;
        }

        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        for (String pkg : packageNames) {
            find(filter, pkg, classes);
        }

        log.debug("Found: " + classes);

        return classes;
    }

    protected void find(PackageScanFilter test, String packageName, Set<Class<?>> classes) {
        packageName = packageName.replace('.', '/');

        Set<ClassLoader> set = getClassLoaders();

        if (!loadedPackages.contains(packageName)) {
            for (ClassLoader classLoader : set) {
                this.findAllClasses(packageName, classLoader);
            }
            loadedPackages.add(packageName);
        }

        findInAllClasses(test, packageName, classes);
    }

    protected void findAllClasses(String packageName, ClassLoader loader) {
        log.debug("Searching for all classes in package: " + packageName + " using classloader: " + loader.getClass().getName());

        Enumeration<URL> urls;
        try {
            urls = getResources(loader, packageName);
            if (!urls.hasMoreElements()) {
                log.debug("No URLs returned by classloader");
            }
        } catch (IOException ioe) {
            log.warning("Cannot read package: " + packageName, ioe);
            return;
        }

        while (urls.hasMoreElements()) {
            URL url = null;
            try {
                url = urls.nextElement();
                log.debug("URL from classloader: " + url);

                url = customResourceLocator(url);

                String urlPath = url.getFile();
                String host = null;
                urlPath = URLDecoder.decode(urlPath, "UTF-8");

                if (url.getProtocol().equals("vfs") && !urlPath.startsWith("vfs")) {
                    urlPath = "vfs:"+urlPath;
                }
                if (url.getProtocol().equals("vfszip") && !urlPath.startsWith("vfszip")) {
                    urlPath = "vfszip:"+urlPath;
                }

                log.debug("Decoded urlPath: " + urlPath + " with protocol: " + url.getProtocol());

                // If it's a file in a directory, trim the stupid file: spec
                if (urlPath.startsWith("file:")) {
                    // file path can be temporary folder which uses characters that the URLDecoder decodes wrong
                    // for example + being decoded to something else (+ can be used in temp folders on Mac OS)
                    // to remedy this then create new path without using the URLDecoder
                    try {
                        URI uri = new URI(url.getFile());
                        host = uri.getHost();
                        urlPath = uri.getPath();
                    } catch (URISyntaxException e) {
                        // fallback to use as it was given from the URLDecoder
                        // this allows us to work on Windows if users have spaces in paths
                    }

                    if (urlPath.startsWith("file:")) {
                        urlPath = urlPath.substring(5);
                    }
                }

                // osgi bundles should be skipped
                if (url.toString().startsWith("bundle:") || urlPath.startsWith("bundle:")) {
                    log.debug("It's a virtual osgi bundle, skipping");
                    continue;
                }

                // Else it's in a JAR, grab the path to the jar
                if (urlPath.contains(".jar/") && !urlPath.contains(".jar!/")) {
                    urlPath = urlPath.replace(".jar/", ".jar!/");
                }

                if (urlPath.indexOf('!') > 0) {
                    urlPath = urlPath.substring(0, urlPath.indexOf('!'));
                }

                // If a host component was given prepend it to the decoded path.
                // This still has its problems as we silently skip user and password
                // information etc. but it fixes UNC urls on windows.
                if (host != null) {
                    if (urlPath.startsWith("/")) {
                        urlPath = "//" + host + urlPath;
                    } else {
                        urlPath = "//" + host + "/" + urlPath;
                    }
                }

                File file = new File(urlPath);
                if (file.isDirectory()) {
                    log.debug("Loading from directory using file: " + file);
                    loadImplementationsInDirectory(packageName, file, loader);
                } else {
                    InputStream stream;
                    if (urlPath.startsWith("http:") || urlPath.startsWith("https:")
                            || urlPath.startsWith("sonicfs:") || urlPath.startsWith("vfs:") || urlPath.startsWith("vfszip:")) {
                        // load resources using http/https
                        // sonic ESB requires to be loaded using a regular URLConnection
                        URL urlStream = new URL(urlPath);
                        log.debug("Loading from jar using "+urlStream.getProtocol()+": " + urlPath);
                        URLConnection con = urlStream.openConnection();
                        // disable cache mainly to avoid jar file locking on Windows
                        con.setUseCaches(false);
                        stream = con.getInputStream();
                    } else {
                        log.debug("Loading from jar using file: " + file);
                        stream = new FileInputStream(file);
                    }

                    try {
                        loadImplementationsInJar(packageName, stream, loader, file);
                    } catch (IOException ioe) {
                        log.warning("Cannot search jar file '" + urlPath + "' for classes due to an IOException: " + ioe.getMessage(), ioe);
                    } finally {
                        stream.close();
                    }
                }
            } catch (IOException e) {
                // use debug logging to avoid being to noisy in logs
                log.debug("Cannot read entries in url: " + url, e);
            }
        }
    }

    protected void findInAllClasses(PackageScanFilter test, String packageName, Set<Class<?>> classes) {
        log.debug("Searching for: " + test + " in package: " + packageName );

        Set<Class> packageClasses = getFoundClasses(packageName);
        if (packageClasses == null) {
            log.debug("No classes found in package: " + packageName );
            return;
        }
        for (Class type : packageClasses) {
            if (test.matches(type)) {
                classes.add(type);
            }
        }

    }

    protected void addFoundClass(Class<?> type) {
        if (type.getPackage() != null) {
            String packageName = type.getPackage().getName();
            List<String> packageNameParts = Arrays.asList(packageName.split("\\."));
            for (int i = 0; i < packageNameParts.size(); i++) {
                String thisPackage = StringUtils.join(packageNameParts.subList(0, i + 1), "/");
                addFoundClass(thisPackage, type);
            }
        }
    }


    protected void addFoundClass(String packageName, Class<?> type) {
        packageName = packageName.replace("/", ".");

        if (!this.allClassesByPackage.containsKey(packageName)) {
            this.allClassesByPackage.put(packageName, new HashSet<Class>());
        }

        this.allClassesByPackage.get(packageName).add(type);
    }


    protected Set<Class> getFoundClasses(String packageName) {
        packageName = packageName.replace("/", ".");
        return this.allClassesByPackage.get(packageName);
    }

    // We can override this method to support the custom ResourceLocator

    protected URL customResourceLocator(URL url) throws IOException {
        // Do nothing here
        return url;
    }

    /**
     * Strategy to get the resources by the given classloader.
     * <p/>
     * Notice that in WebSphere platforms there is a {@link WebSpherePackageScanClassResolver}
     * to take care of WebSphere's odditiy of resource loading.
     *
     * @param loader      the classloader
     * @param packageName the packagename for the package to load
     * @return URL's for the given package
     * @throws IOException is thrown by the classloader
     */
    protected Enumeration<URL> getResources(ClassLoader loader, String packageName) throws IOException {
        log.debug("Getting resource URL for package: " + packageName + " with classloader: " + loader);

        // If the URL is a jar, the URLClassloader.getResources() seems to require a trailing slash.  The
        // trailing slash is harmless for other URLs
        if (!packageName.endsWith("/")) {
            packageName = packageName + "/";
        }
        return loader.getResources(packageName);
    }

    private PackageScanFilter getCompositeFilter(PackageScanFilter filter) {
        if (scanFilters != null) {
            CompositePackageScanFilter composite = new CompositePackageScanFilter(scanFilters);
            composite.addFilter(filter);
            return composite;
        }
        return filter;
    }

    /**
     * Finds matches in a physical directory on a filesystem. Examines all files
     * within a directory - if the File object is not a directory, and ends with
     * <i>.class</i> the file is loaded. Operates recursively to find classes within a
     * folder structure matching the package structure.
     *
     * @param parent   the package name up to this directory in the package
     *                 hierarchy. E.g. if /classes is in the classpath and we wish to
     *                 examine files in /classes/org/apache then the values of
     *                 <i>parent</i> would be <i>org/apache</i>
     * @param location a File object representing a directory
     */
    private void loadImplementationsInDirectory(String parent, File location, ClassLoader classLoader) {
        Set<String> classFiles = classFilesByLocation.get(location.toString());
        if (classFiles == null) {
            classFiles = new HashSet<String>();

            File[] files = location.listFiles();
            StringBuilder builder = null;

            for (File file : files) {
                builder = new StringBuilder(100);
                String name = file.getName();
                if (name != null) {
                    name = name.trim();
                    builder.append(parent).append("/").append(name);
                    String packageOrClass = parent == null ? name : builder.toString();

                    if (file.isDirectory()) {
                        loadImplementationsInDirectory(packageOrClass, file, classLoader);
                    } else if (name.endsWith(".class")) {
                        classFiles.add(packageOrClass);
                    }
                }
            }
        }

        for (String packageOrClass : classFiles) {
            this.loadClass(packageOrClass, classLoader);
        }
    }

    private void loadClass(String className, ClassLoader classLoader) {
        try {
            String externalName = className.substring(0, className.indexOf('.')).replace('/', '.');
            Class<?> type = classLoader.loadClass(externalName);
            log.debug("Loaded the class: " + type + " in classloader: " + classLoader);

            if (Modifier.isAbstract(type.getModifiers()) || Modifier.isInterface(type.getModifiers())) {
                return;
            }

            addFoundClass(type);

        } catch (ClassNotFoundException e) {
            log.debug("Cannot find class '" + className + "' in classloader: " + classLoader
                    + ". Reason: " + e, e);
        } catch (NoClassDefFoundError e) {
            log.debug("Cannot find the class definition '" + className + "' in classloader: " + classLoader
                    + ". Reason: " + e, e);
        } catch (LinkageError e) {
            log.debug("Cannot find the class definition '" + className + "' in classloader: " + classLoader
                    + ". Reason: " + e, e);
        } catch (Throwable e) {
            log.severe("Cannot load class '"+className+"' in classloader: "+classLoader+".  Reason: "+e, e);
        }

    }

    /**
     * Finds matching classes within a jar files that contains a folder
     * structure matching the package structure. If the File is not a JarFile or
     * does not exist a warning will be logged, but no error will be raised.
     *
     * @param parent  the parent package under which classes must be in order to
     *                be considered
     * @param stream  the inputstream of the jar file to be examined for classes
     */
    protected void loadImplementationsInJar(String parent, InputStream stream, ClassLoader loader, File parentFile) throws IOException {
        Set<String> classFiles = classFilesByLocation.get(parentFile.toString());

        if (classFiles == null) {
            classFiles = new HashSet<String>();
            classFilesByLocation.put(parentFile.toString(), classFiles);
            JarInputStream jarStream = null;
            if (stream instanceof JarInputStream) {
                jarStream = (JarInputStream) stream;
            } else {
                jarStream = new JarInputStream(stream);
            }

            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (name != null) {
                    if (name.endsWith(".jar")) { //in a nested jar
                        log.debug("Found nested jar " + name);
                        File unzippedParent = unzippedJars.get(parentFile);
                        if (unzippedParent == null) {
                            unzippedParent = FileUtil.unzip(parentFile);
                            unzippedJars.put(parentFile, unzippedParent);
                        }
                        File nestedJar = new File(unzippedParent, name);
                        JarInputStream nestedJarStream = new JarInputStream(new FileInputStream(nestedJar));
                        try {
                            loadImplementationsInJar(parent, nestedJarStream, loader, nestedJar);
                        } finally {
                            nestedJarStream.close();
                        }
                    } else if (!entry.isDirectory() && name.endsWith(".class")) {
                        classFiles.add(name.trim());
                    }
                }
            }
        }

        for (String name : classFiles) {
            if (name.contains(parent)) {
                loadClass(name, loader);
            }
        }
    }

    /**
     * Add the class designated by the fully qualified class name provided to
     * the set of resolved classes if and only if it is approved by the Test
     * supplied.
     *
     * @param test the test used to determine if the class matches
     * @param fqn  the fully qualified name of a class
     */
    protected void addIfMatching(PackageScanFilter test, String fqn, Set<Class<?>> classes) {
        try {
            String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
            Set<ClassLoader> set = getClassLoaders();
            boolean found = false;
            for (ClassLoader classLoader : set) {
                log.debug("Testing that class " + externalName + " matches criteria [" + test + "] using classloader:" + classLoader);
                try {
                    Class<?> type = classLoader.loadClass(externalName);
                    log.debug("Loaded the class: " + type + " in classloader: " + classLoader);
                    if (test.matches(type)) {
                        log.debug("Found class: " + type + " which matches the filter in classloader: " + classLoader);
                        classes.add(type);
                    }
                    found = true;
                    break;
                } catch (ClassNotFoundException e) {
                    log.debug("Cannot find class '" + fqn + "' in classloader: " + classLoader
                            + ". Reason: " + e, e);
                } catch (NoClassDefFoundError e) {
                    log.debug("Cannot find the class definition '" + fqn + "' in classloader: " + classLoader
                            + ". Reason: " + e, e);
                } catch (LinkageError e) {
                    log.debug("Cannot find the class definition '" + fqn + "' in classloader: " + classLoader
                            + ". Reason: " + e, e);
                } catch (Throwable e) {
                    log.severe("Cannot load class '"+fqn+"' in classloader: "+classLoader+".  Reason: "+e, e);
                }
            }
            if (!found) {
                // use debug to avoid being noisy in logs
                log.debug("Cannot find class '" + fqn + "' in any classloaders: " + set);
            }
        } catch (Exception e) {
            log.warning("Cannot examine class '" + fqn + "' due to a " + e.getClass().getName()
                    + " with message: " + e.getMessage(), e);
        }
    }

}
