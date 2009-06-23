package liquibase.util.plugin;

import liquibase.util.log.LogFactory;
import liquibase.util.StringUtils;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.exception.UnexpectedLiquibaseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

public class ClassPathScanner {

    private static ClassPathScanner instance = new ClassPathScanner();

    private ResourceAccessor resourceAccessor;

    private Map<Class, List<Class>> classesBySuperclass;
    private List<String> packagesToScan;

    private ClassPathScanner() {
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    public static ClassPathScanner getInstance() {
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
                        packagesToScan.add(value.toString());
                    }
                }
                is.close();
            }
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Class[] getClasses(Class requiredInterface) throws Exception {
        Class.forName(requiredInterface.getName());

        if (!classesBySuperclass.containsKey(requiredInterface)) {
            classesBySuperclass.put(requiredInterface, new ArrayList<Class>());

            for (String packageName : packagesToScan) {
                String path = packageName.replace('.', '/');
                Enumeration<URL> resources = resourceAccessor.getResources(path);
                while (resources.hasMoreElements()) {
                    classesBySuperclass.get(requiredInterface).addAll(findClasses(resources.nextElement(), packageName, requiredInterface));
                }
            }
        }

        List<Class> classes = classesBySuperclass.get(requiredInterface);
        return classes.toArray(new Class[classes.size()]);
    }

    private List<Class> findClasses(URL resource, String packageName, Class requiredInterface) throws Exception {
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
                System.out.println(directory + " does not exist");
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
            Class<?> clazz = Class.forName(potentialClassName, true, resourceAccessor.toClassLoader());
            if (!clazz.isInterface()
                    && !Modifier.isAbstract(clazz.getModifiers())
                    && isCorrectType(clazz, requiredInterface)) {
                try {
                    clazz.getConstructor();
                    classes.add(clazz);
                } catch (NoSuchMethodException e) {
                    URL classAsUrl = resourceAccessor.toClassLoader().getResource(clazz.getName().replaceAll("\\.", "/") + ".class");
                    if (!clazz.getName().equals("liquibase.database.core.HibernateDatabase") && (classAsUrl != null && !classAsUrl.toExternalForm().contains("build-test/liquibase/"))) { //keeps the logs down
                        LogFactory.getLogger().warning("Class " + clazz.getName() + " does not have a public no-arg constructor, so it can't be used as a " + requiredInterface.getName() + " plug-in");
                    }
                }
            }

        }

        return classes;
    }

    private boolean isCorrectType(Class<?> clazz, Class requiredInterface) {
        return !clazz.equals(Object.class) && (requiredInterface.isAssignableFrom(clazz) || isCorrectType(clazz.getSuperclass(), requiredInterface));
    }

    public static void reset() {
        instance = new ClassPathScanner();
    }
}
