package liquibase.util;

import liquibase.util.log.LogFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PluginUtil {

    public static Class[] getClasses(String packageName, Class requiredInterface) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile().replace("%20", " ")));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName, requiredInterface));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    private static List<Class> findClasses(File directory, String packageName, Class requiredInterface) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName(), requiredInterface));
            } else if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (!clazz.isInterface()
                        && !Modifier.isAbstract(clazz.getModifiers())
                        && isCorrectType(clazz, requiredInterface)) {
                    try {
                        clazz.getConstructor();
                        classes.add(clazz);
                    } catch (NoSuchMethodException e) {
                            LogFactory.getLogger().warning("Class "+clazz.getName()+" does not have a public no-arg constructor, so it can't be used as a "+requiredInterface.getName()+" plug-in");
                    }
                }


            }
        }
        return classes;
    }

    private static boolean isCorrectType(Class<?> clazz, Class requiredInterface) {
        return !clazz.equals(Object.class) && (requiredInterface.isAssignableFrom(clazz) || isCorrectType(clazz.getSuperclass(), requiredInterface));
    }

}
