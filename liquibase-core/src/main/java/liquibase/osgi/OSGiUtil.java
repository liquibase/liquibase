package liquibase.osgi;

import org.osgi.framework.BundleReference;

public final class OSGiUtil {

    private static volatile Boolean loadedAsBundle;

    public static boolean isLiquibaseLoadedAsOSGiBundle() {
        if (loadedAsBundle == null) {
            ClassLoader classLoader = OSGiUtil.class.getClassLoader();
            try {
                classLoader.loadClass("org.osgi.framework.BundleReference");
            } catch (ClassNotFoundException e) {
                return false;
            }

            if (classLoader instanceof BundleReference) {
                loadedAsBundle = true;
            } else {
                loadedAsBundle =false;
            }
        }
        return loadedAsBundle;
    }

    private OSGiUtil() {
    }
}
