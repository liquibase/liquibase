package liquibase.servicelocator;

import liquibase.exception.ServiceNotFoundException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.osgi.OSGiPackageScanClassResolver;
import liquibase.osgi.OSGiResourceAccessor;
import liquibase.osgi.OSGiUtil;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.Manifest;

/**
 * Entry point to the Liquibase specific ServiceLocator framework.
 *
 * Services (concrete instances of interfaces) are located by scanning nominated
 * packages on the classpath for implementations of the interface.
 */
public class ServiceLocator {

    private static ServiceLocator instance;

    static {
        try {
            // 加载出错
            Class<?> scanner = Class.forName("Liquibase.ServiceLocator.ClrServiceLocator, Liquibase");
            instance = (ServiceLocator) scanner.newInstance();
        } catch (Exception e) {
            try {
                // 是否OSGI
                if (OSGiUtil.isLiquibaseLoadedAsOSGiBundle()) {
                    Bundle liquibaseBundle = FrameworkUtil.getBundle(ServiceLocator.class);
                    instance = new ServiceLocator(new OSGiPackageScanClassResolver(liquibaseBundle), 
                            new OSGiResourceAccessor(liquibaseBundle));
                } else {
                    // 创建ServiceLocator
                    instance = new ServiceLocator();
                }
            } catch (Throwable e1) {
                LogService.getLog(ServiceLocator.class).severe(LogType.LOG, "Cannot build ServiceLocator", e1);
            }
        }
    }

    // 资源访问授权
    private ResourceAccessor resourceAccessor;

    private Map<Class, List<Class>> classesBySuperclass;
    // 要扫描的package
    private List<String> packagesToScan;
    // 扫描解析器
    private PackageScanClassResolver classResolver;

    protected ServiceLocator() {
        // 设置:PackageScanClassResolver
        this.classResolver = defaultClassLoader();
        // **************************************************************************
        // 设置要在哪些package下扫描
        // 首先通过环境变量读取要加载的目录:liquibase.scan.package(多个package之间用逗号分隔),如果,加载不了,则读取:META-INF/MANIFEST.MF --> Liquibase-Package进行加载,如果再加载不了,就手工指定要加载的package.
        // **************************************************************************
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

    public static synchronized void setInstance(ServiceLocator newInstance) {
        instance = newInstance;
    }

    public static synchronized void reset() {
        instance = new ServiceLocator();
    }

    protected PackageScanClassResolver defaultClassLoader(){
        // 判断是否为:WebSphere
        if (WebSpherePackageScanClassResolver.isWebSphereClassLoader(this.getClass().getClassLoader())) { // false
            LogService.getLog(getClass()).debug(LogType.LOG, "Using WebSphere Specific Class Resolver");
            return new WebSpherePackageScanClassResolver("liquibase/parser/core/xml/dbchangelog-2.0.xsd");
        } else {
            // 创建默认的包扫描解析器
            return new DefaultPackageScanClassResolver();
        }
    }

    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
        this.classesBySuperclass = new HashMap<>();

        // 设置ClassLoader
        this.classResolver.setClassLoaders(new HashSet<>(Arrays.asList(new ClassLoader[]{resourceAccessor.toClassLoader()})));

        if (packagesToScan == null) {  // 如果没有加载过,则进行加载.
            packagesToScan = new ArrayList<>();
            // 1. 读取环境变量里的信息进行加载
            String packagesToScanSystemProp = System.getProperty("liquibase.scan.packages");
            if ((packagesToScanSystemProp != null) &&
                ((packagesToScanSystemProp = StringUtils.trimToNull(packagesToScanSystemProp)) != null)) {
                // 多个之间逗号分隔
                for (String value : packagesToScanSystemProp.split(",")) {
                    addPackageToScan(value);
                }
            } else {
                Set<InputStream> manifests;
                try {
                    // 2. 读取:META-INF/MANIFEST.MF --> Liquibase-Package加载
                    manifests = resourceAccessor.getResourcesAsStream("META-INF/MANIFEST.MF");
                    if (manifests != null) {
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
                    }
                } catch (IOException e) {
                    throw new UnexpectedLiquibaseException(e);
                }

                if (packagesToScan.isEmpty()) {  // 3. 最后手工指定要加载的包名称
                    addPackageToScan("liquibase.change");
                    addPackageToScan("liquibase.changelog");
                    addPackageToScan("liquibase.command");
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
                    addPackageToScan("liquibase.sdk.database");
                    addPackageToScan("liquibase.ext");
                }
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

                if ((returnObject == null) || (newInstance.getPriority() > returnObject.getPriority())) {
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

    /**
     * 根据类名称,找到符合该类的所有实现类
     * @param requiredInterface
     * @param <T>
     * @return
     * @throws ServiceNotFoundException
     */
    public <T> Class<? extends T>[] findClasses(Class<T> requiredInterface) throws ServiceNotFoundException {
        LogService.getLog(getClass()).debug(LogType.LOG, "ServiceLocator.findClasses for "+requiredInterface.getName());

            try {
                Class.forName(requiredInterface.getName());

                // 判断是否扫描加载过了
                if (!classesBySuperclass.containsKey(requiredInterface)) {
                    // ********************************************************************
                    // findClassesImpl根据类,找到所有符合的子类
                    // ********************************************************************
                    classesBySuperclass.put(requiredInterface, findClassesImpl(requiredInterface));
                }
            } catch (Exception e) {
                throw new ServiceNotFoundException(e);
            }

        List<Class> classes = classesBySuperclass.get(requiredInterface);
        HashSet<Class> uniqueClasses = new HashSet<>(classes);
        return uniqueClasses.toArray(new Class[uniqueClasses.size()]);
    }

    public Object newInstance(Class requiredInterface) throws ServiceNotFoundException {
        try {
            return findClass(requiredInterface).newInstance();
        } catch (Exception e) {
            throw new ServiceNotFoundException(e);
        }
    }

    /**
     *
     * @param requiredInterface   例如:  liquibase.database.Database
     * @return
     * @throws Exception
     */
    private List<Class> findClassesImpl(Class requiredInterface) throws Exception {
        LogService.getLog(getClass()).debug(LogType.LOG, "ServiceLocator finding classes matching interface " + requiredInterface.getName());

        List<Class> classes = new ArrayList<>();

        // 再次设置了ClassLoader
        classResolver.addClassLoader(resourceAccessor.toClassLoader());

        // *************************************************************************************
        // 调用:DefaultPackageScanClassResolver.findImplementations(liquibase.database.Database, new String[]{ "liquibase.change","liquibase.changelog","liquibase.command" });
        // 在指定的package下扫描,所有符合该类的子类.
        // *************************************************************************************
        for (Class<?> clazz : classResolver.findImplementations(requiredInterface, packagesToScan.toArray(new String[packagesToScan.size()]))) {
            if ((clazz.getAnnotation(LiquibaseService.class) != null) && clazz.getAnnotation(LiquibaseService.class)
                .skip()) {
                continue;
            }

            if (!Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers()) && !clazz.isAnonymousClass() &&!clazz.isSynthetic() && Modifier.isPublic(clazz.getModifiers())) {
                try {
                    clazz.getConstructor();
                    LogService.getLog(getClass()).debug(LogType.LOG, clazz.getName() + " matches "+requiredInterface.getName());

                    classes.add(clazz);
                } catch (NoSuchMethodException e) {
                    LogService.getLog(getClass()).info(LogType.LOG, "Can not use " + clazz + " as a Liquibase service because it does not have a " +
                        "no-argument constructor");
                } catch (NoClassDefFoundError e) {
                    String message = "Can not use " + clazz + " as a Liquibase service because " + e.getMessage()
                        .replace("/", ".") + " is not in the classpath";
                    if (e.getMessage().startsWith("org/yaml/snakeyaml")) {
                        LogService.getLog(getClass()).info(LogType.LOG, message);
                    } else {
                        LogService.getLog(getClass()).warning(LogType.LOG, message);
                    }
                }
            }
        }

        return classes;
    }
}
