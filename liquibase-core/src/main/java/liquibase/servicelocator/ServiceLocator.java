package liquibase.servicelocator;

import liquibase.exception.ServiceNotFoundException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.logging.core.DefaultLogger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;

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
    private PackageScanClassResolver classResolver;

    protected ServiceLocator() {
        this.classResolver = defaultClassLoader();
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    protected ServiceLocator(ResourceAccessor accessor) {
        this.classResolver = defaultClassLoader();
        setResourceAccessor(accessor);
    }

    protected ServiceLocator(PackageScanClassResolver classResolver) {
        this.classResolver = classResolver;
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    protected ServiceLocator(PackageScanClassResolver classResolver, ResourceAccessor accessor) {
        this.classResolver = classResolver;
        setResourceAccessor(accessor);
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    public static void setInstance(ServiceLocator newInstance) {
        instance = newInstance;
    }

    protected PackageScanClassResolver defaultClassLoader(){
        if (WebSpherePackageScanClassResolver.isWebSphereClassLoader(this.getClass().getClassLoader())) {
            logger.debug("Using WebSphere Specific Class Resolver");
            return new WebSpherePackageScanClassResolver("liquibase/parser/core/xml/dbchangelog-2.0.xsd");
        } else {
            return new DefaultPackageScanClassResolver();
        }
    }

    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
        this.classesBySuperclass = new HashMap<Class, List<Class>>();

        this.classResolver.setClassLoaders(new HashSet<ClassLoader>(Arrays.asList(new ClassLoader[] {resourceAccessor.toClassLoader()})));

        packagesToScan = new ArrayList<String>();
        String packagesToScanSystemProp = System.getProperty("liquibase.scan.packages");
        if ((packagesToScanSystemProp != null) &&
        	((packagesToScanSystemProp = StringUtils.trimToNull(packagesToScanSystemProp)) != null)) {
        	for (String value : packagesToScanSystemProp.split(",")) {
        		addPackageToScan(value);
        	}
        } else {
	        Set<InputStream> manifests;
	        try {
	            manifests = resourceAccessor.getResourcesAsStream("META-INF/MANIFEST.MF");
	            for (InputStream is : manifests) {
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

            if (packagesToScan.size() == 0) {
                addPackageToScan("liquibase.change");
                addPackageToScan("liquibase.changelog");
                addPackageToScan("liquibase.database");
                addPackageToScan("liquibase.parser");
                addPackageToScan("liquibase.precondition");
                addPackageToScan("liquibase.datatype");
                addPackageToScan("liquibase.serializer");
                addPackageToScan("liquibase.sqlgenerator");
                addPackageToScan("liquibase.executor");
                addPackageToScan("liquibase.snapshot");
                addPackageToScan("liquibase.logging");
                addPackageToScan("liquibase.diff");
                addPackageToScan("liquibase.structure");
                addPackageToScan("liquibase.structurecompare");
                addPackageToScan("liquibase.lockservice");
                addPackageToScan("liquibase.sdk");
                addPackageToScan("liquibase.ext");
            }
        }
    }

    public void addPackageToScan(String packageName) {
        packagesToScan.add(packageName);
    }

    public List<String> getPackages() {
        return packagesToScan;
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

    public <T> Class<? extends T>[] findClasses(Class<T> requiredInterface) throws ServiceNotFoundException {
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

        classResolver.addClassLoader(resourceAccessor.toClassLoader());
        for (Class<?> clazz : classResolver.findImplementations(requiredInterface, packagesToScan.toArray(new String[packagesToScan.size()]))) {
            if (clazz.getAnnotation(LiquibaseService.class ) != null  && clazz.getAnnotation(LiquibaseService.class).skip()) {
                continue;
            }

            if (!Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers()) && Modifier.isPublic(clazz.getModifiers())) {
                try {
                    clazz.getConstructor();
                    logger.debug(clazz.getName() + " matches "+requiredInterface.getName());

                    classes.add(clazz);
                } catch (NoSuchMethodException e) {
                    logger.info("Can not use "+clazz+" as a Liquibase service because it does not have a no-argument constructor" );
                } catch (NoClassDefFoundError e) {
                    String message = "Can not use " + clazz + " as a Liquibase service because " + e.getMessage().replace("/", ".") + " is not in the classpath";
                    if (e.getMessage().startsWith("org/yaml/snakeyaml")) {
                        logger.info(message);
                    } else {
                        logger.warning(message);
                    }
                }
            }
        }

        return classes;
    }

    public static void reset() {
        instance = new ServiceLocator();
    }

    protected Logger getLogger() {
        return logger;
    }
}
