package liquibase.sdk;

import liquibase.change.Change;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.sqlgenerator.SqlGenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

public class Context {

    public static final String LIQUIBASE_SDK_PROPERTIES_FILENAME = "liquibase.sdk.properties";
    private static final List<Class<?>> extensionInterfaces = Arrays.asList(Change.class, SqlGenerator.class);
    private static Context instance;
    private boolean initialized;

    private Set<Class> allClasses = new HashSet<>();
    private Map<Class, Set<Class>> seenExtensionClasses = new HashMap<>();

    private Set<File> propertyFiles;

    private Context() {
    }

    public static synchronized void reset() {
        instance = null;
    }

    public static synchronized Context getInstance() {
        if (instance == null) {
            instance = new Context();
            try {
                instance.init();
            } catch (Exception e) {
                System.out.println("Error initializing context: "+e.getMessage());
                e.printStackTrace();
            }

        }
        return instance;
    }

   public boolean isInitialized() {
        return initialized;
    }

    public Set<Class> getAllClasses() {
        return allClasses;
    }

    public Map<Class, Set<Class>> getSeenExtensionClasses() {
        return seenExtensionClasses;
    }


    protected void init() throws Exception {
        propertyFiles = new HashSet<>();


        Enumeration<URL> resourceUrls = Context.class.getClassLoader().getResources(LIQUIBASE_SDK_PROPERTIES_FILENAME);
        while(resourceUrls.hasMoreElements()) {
            File propertiesFile = new File(resourceUrls.nextElement().toURI());
            propertyFiles.add(propertiesFile);
        }

        try {
            for (String packageName : ServiceLocator.getInstance().getPackages()) {
                Enumeration<URL> dirs = this.getClass().getClassLoader().getResources(packageName.replace('.', '/'));
                while (dirs.hasMoreElements()) {
                    File dir = new File(dirs.nextElement().toURI());
                    findClasses(packageName, dir);
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseSdkException(e);
        }

        for (Class clazz : allClasses) {
            if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) {
                continue;
            }
            Class type = getExtensionType(clazz);
            if (type != null) {
                if (!seenExtensionClasses.containsKey(type)) {
                    seenExtensionClasses.put(type, new HashSet<Class>());
                }
                seenExtensionClasses.get(type).add(clazz);
            }
        }

        this.initialized = true;
    }

    private Class getExtensionType(Class clazz) {
        for (Class type : clazz.getInterfaces()) {
            if (extensionInterfaces.contains(type)) {
                return type;
            }
        }
        Class superclass = clazz.getSuperclass();
        if (superclass == null) {
            return null;
        }
        return getExtensionType(superclass);
    }

    private void findClasses(String packageName, File dir) throws ClassNotFoundException {
        packageName = packageName.replaceFirst("^\\.","");
        String[] classFiles = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });
        for (String classFile : classFiles) {
            Class<?> foundClass = null;
            String className = packageName + "." + classFile.replaceFirst("\\.class$", "");
            try {
                foundClass = Class.forName(className);
                allClasses.add(foundClass);
            } catch (Exception e) {
                System.out.println("Error loading class "+className+": "+e.getCause().getClass().getName()+": "+e.getCause().getMessage());
            }
        }

        File[] subDirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File subDir : subDirs) {
            findClasses(packageName+"."+subDir.getName(), subDir);
        }

    }
}