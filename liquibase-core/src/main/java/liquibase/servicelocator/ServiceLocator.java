package liquibase.servicelocator;

import liquibase.exception.ServiceNotFoundException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.logging.core.DefaultLogger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

public class ServiceLocator {

    private static ServiceLocator instance;

    static {
        try {
            Class<?> scanner = Class.forName("Liquibase.ServiceLocator.ClrServiceLocator, Liquibase");
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
                String attributes = StringUtils.trimToNull(manifest.getMainAttributes().getValue("Liquibase-Package"));
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
        if (PrioritizedService.class.isAssignableFrom(requiredInterface)) {
            PrioritizedService returnObject = null;
            for (Class clazz : classes) {
                PrioritizedService newInstance;
                try {
                    newInstance = (PrioritizedService) clazz.newInstance();
                } catch (Exception e) {
                    throw new UnexpectedLiquibaseException(e);
                }

                if (returnObject == null || newInstance.getPriority() > returnObject.getPriority()) {
                    returnObject = newInstance;
                }
            }

            if (returnObject == null) {
                throw new ServiceNotFoundException("Could not find implementation of " + requiredInterface.getName());
            }
            return returnObject.getClass();
        }

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
                    classesBySuperclass.put(requiredInterface, findClassesImpl(requiredInterface));
                }
            } catch (Exception e) {
                throw new ServiceNotFoundException(e);
            }

        List<Class> classes = classesBySuperclass.get(requiredInterface);
        HashSet<Class> uniqueClasses = new HashSet<Class>(classes);
        return uniqueClasses.toArray(new Class[uniqueClasses.size()]);
    }

    public Object newInstance(Class requiredInterface) throws ServiceNotFoundException {
        try {
            return findClass(requiredInterface).newInstance();
        } catch (Exception e) {
            throw new ServiceNotFoundException(e);
        }
    }

    private List<Class> findClassesImpl(Class requiredInterface) throws Exception {
        logger.debug("ServiceLocator finding classes matching interface " + requiredInterface.getName());

        List<Class> classes = new ArrayList<Class>();

        ResolverUtil resolverUtil = new ResolverUtil();
        resolverUtil.setClassLoader(resourceAccessor.toClassLoader());          
        for (Class clazz : (Set<Class>) resolverUtil.findImplementations(requiredInterface, packagesToScan.toArray(new String[packagesToScan.size()])).getClasses()) {
            if (!Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers()) && Modifier.isPublic(clazz.getModifiers())) {
                try {
                    clazz.getConstructor();
                    logger.debug(clazz.getName() + " matches "+requiredInterface.getName());

                    classes.add(clazz);
                } catch (NoSuchMethodException e) {
                    logger.info("Can not use "+clazz+" as a Liquibase service because it does not have a default constructor" );
                }
            }
        }

        return classes;
    }

    public static void reset() {
        instance = new ServiceLocator();
    }
}
