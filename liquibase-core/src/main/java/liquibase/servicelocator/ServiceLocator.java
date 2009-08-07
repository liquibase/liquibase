package liquibase.servicelocator;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ServiceNotFoundException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.logging.core.DefaultLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ServiceLocator {

    private static ServiceLocator instance;

    static {
        try {
            Class<?> scanner = Class.forName("LiquiBase.ServiceLocator.ClrServiceLocator, LiquiBase");
            instance = (ServiceLocator) scanner.newInstance();
        } catch (Exception e) {
            instance = new ServiceLocator();
        }
    }

    private ResourceAccessor resourceAccessor;

    private Map<Class, List<Class>> classesBySuperclass;
    private List<String> packagesToScan;
    private Logger logger = new DefaultLogger(); //cannot look up regular logger because you get a stackoverflow since we are in the servicelocator

    protected ServiceLocator() {
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    protected ServiceLocator(ResourceAccessor accessor) {
        setResourceAccessor(accessor);
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
        this.classesBySuperclass = new HashMap<Class, List<Class>>();

        packagesToScan = new ArrayList<String>();
        Enumeration<URL> manifests = null;
        try {
            manifests = resourceAccessor.getResources("META-INF/MANIFEST.MF");
            while (manifests.hasMoreElements()) {
                URL url = manifests.nextElement();
                InputStream is = url.openStream();
                Manifest manifest = new Manifest(is);
                String attributes = StringUtils.trimToNull(manifest.getMainAttributes().getValue("LiquiBase-Package"));
                if (attributes != null) {
                    for (Object value : attributes.split(",")) {
                        addPackageToScan(value.toString());
                    }
                }
                is.close();
            }
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void addPackageToScan(String packageName) {
        packagesToScan.add(packageName);
    }

    public Class findClass(Class requiredInterface) throws ServiceNotFoundException {
        Class[] classes = findClasses(requiredInterface);
        if (classes.length != 1) {
            throw new ServiceNotFoundException("Could not find unique implementation of " + requiredInterface.getName() + ".  Found " + classes.length + " implementations");
        }

        return classes[0];
    }

    public Class[] findClasses(Class requiredInterface) throws ServiceNotFoundException {
        logger.debug("ServiceLocator.findClasses for "+requiredInterface.getName());

            try {
                Class.forName(requiredInterface.getName());

                if (!classesBySuperclass.containsKey(requiredInterface)) {
                    classesBySuperclass.put(requiredInterface, new ArrayList<Class>());

                    for (String packageName : packagesToScan) {
                        logger.debug("ServiceLocator scanning "+packageName+" with resoureAccessor "+resourceAccessor.getClass().getName());
                        String path = packageName.replace('.', '/');
                        Enumeration<URL> resources = resourceAccessor.getResources(path);
                        while (resources.hasMoreElements()) {
                            URL resource = resources.nextElement();
                            logger.debug("Found "+packageName+" in "+resource.toExternalForm());
                            classesBySuperclass.get(requiredInterface).addAll(findClasses(resource, packageName, requiredInterface));
                        }
                    }
                }
            } catch (Exception e) {
                throw new ServiceNotFoundException(e);
            }

        List<Class> classes = classesBySuperclass.get(requiredInterface);
        return classes.toArray(new Class[classes.size()]);
    }

    public Object createInstance(Class requiredInterface) throws ServiceNotFoundException {
        try {
            return findClass(requiredInterface).newInstance();
        } catch (Exception e) {
            throw new ServiceNotFoundException(e);
        }
    }

    private List<Class> findClasses(URL resource, String packageName, Class requiredInterface) throws Exception {
        logger.debug("ServiceLocator finding " + packageName + " classes in " + resource.toExternalForm() + " matching interface " + requiredInterface.getName());
        List<Class> classes = new ArrayList<Class>();
//        if (directory.toURI().toString().startsWith("jar:")) {
//            System.out.println("have a jar: "+directory.toString());
//        }

        List<String> potentialClassNames = new ArrayList<String>();
        if (resource.getProtocol().equals("jar")) {
            File zipfile = new File(resource.getFile().split("!")[0].replaceFirst("file:\\/", ""));
            JarFile jarFile = new JarFile(zipfile);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(packageName.replaceAll("\\.", "/")) && entry.getName().endsWith(".class")) {
                    potentialClassNames.add(entry.getName().replaceAll("\\/", ".").substring(0, entry.getName().length() - ".class".length()));
                }
            }
        } else if (resource.getProtocol().equals("file")) {
            File directory = new File(resource.getFile().replace("%20", " "));

            if (!directory.exists()) {
//                System.out.println(directory + " does not exist");
                return classes;
            }

            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    assert !file.getName().contains(".");
                    classes.addAll(findClasses(file.toURL(), packageName + "." + file.getName(), requiredInterface));
                } else if (file.getName().endsWith(".class")) {
                    potentialClassNames.add(packageName + '.' + file.getName().substring(0, file.getName().length() - ".class".length()));
                }
            }

        } else {
            throw new UnexpectedLiquibaseException("Cannot read plugin classes from protocol " + resource.getProtocol());
        }

        for (String potentialClassName : potentialClassNames) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(potentialClassName, true, resourceAccessor.toClassLoader());
            } catch (NoClassDefFoundError e) {
                logger.warning("Could not configure extension class " + potentialClassName + ": Missing dependency " + e.getMessage());
                continue;
            } catch (Throwable e) {
                logger.warning("Could not configure extension class " + potentialClassName + ": " + e.getMessage());
                continue;
            }
            if (!clazz.isInterface()
                    && !Modifier.isAbstract(clazz.getModifiers())
                    && isCorrectType(clazz, requiredInterface)) {
                logger.debug(potentialClassName + " matches "+requiredInterface.getName());
                try {
                    clazz.getConstructor();
                    classes.add(clazz);
                } catch (NoSuchMethodException e) {
                    URL classAsUrl = resourceAccessor.toClassLoader().getResource(clazz.getName().replaceAll("\\.", "/") + ".class");
                    if (!clazz.getName().equals("liquibase.database.core.HibernateDatabase")
                            && !clazz.getName().equals("liquibase.executor.LoggingExecutor")
                            && (classAsUrl != null && !classAsUrl.toExternalForm().contains("build-test/liquibase/"))) { //keeps the logs down
                        logger.warning("Class " + clazz.getName() + " does not have a public no-arg constructor, so it can't be used as a " + requiredInterface.getName() + " service");
                    }
                }
//            } else {
//                System.out.println(potentialClassName + " does not match");
            }

        }

        return classes;
    }

    private boolean isCorrectType(Class<?> clazz, Class requiredInterface) {
        return !clazz.equals(Object.class) && (requiredInterface.isAssignableFrom(clazz) || isCorrectType(clazz.getSuperclass(), requiredInterface));
    }

    public static void reset() {
        instance = new ServiceLocator();
    }
}
