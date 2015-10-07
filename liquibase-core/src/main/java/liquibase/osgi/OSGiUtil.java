package liquibase.osgi;

import org.osgi.framework.BundleReference;

public final class OSGiUtil {

    public static boolean isLiquibaseLoadedAsOSGiBundle() {
        ClassLoader classLoader = OSGiUtil.class.getClassLoader();
        try {
            classLoader.loadClass("org.osgi.framework.BundleReference");
        } catch (ClassNotFoundException e) {
            return false;
        }
        
        if (classLoader instanceof BundleReference) {
            return true;
        } else {
            return false;
        }
    }

    private OSGiUtil() {
    }
}
