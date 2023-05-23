package liquibase.util;

import java.util.List;
import java.util.stream.Collectors;
import liquibase.osgi.Activator;
import liquibase.osgi.Activator.LiquibaseBundle;

public final class OsgiUtil {

    private OsgiUtil() {
    }

    /**
     * Tries to load a class under OSGI environment. It will attempt to load the class
     * from all Liquibase bundles registered via
     * {@link Activator Activator}.
     *
     * @param <T>       the type of the class to load
     * @param className the name of the class to load
     * @return the loaded class
     * @throws ClassNotFoundException if the class could not be found
     */
    public static <T> Class<T> loadClass(String className) throws ClassNotFoundException {
        List<LiquibaseBundle> liquibaseBundles = Activator.getLiquibaseBundles();
        for (LiquibaseBundle lb : liquibaseBundles) {
            try {
                Class<T> clazz = (Class<T>) lb.getBundle().loadClass(className);
                if (!isClassAllowed(lb, clazz)) {
                    throw new ClassNotFoundException("Class is not allowed to load, class:" + className + " bundles:"
                            + liquibaseBundles.stream().map(i -> i.getBundle().getSymbolicName())
                                    .collect(Collectors.joining(",")));
                }
                return clazz;
            } catch (ClassNotFoundException ex) {
                // nothing to do
            }
        }
        throw new ClassNotFoundException("Cannot find class:" + className + " bundles:"
                + liquibaseBundles.stream().map(i -> i.getBundle().getSymbolicName())
                        .collect(Collectors.joining(",")));
    }

    /**
     * Checks whether a given class is allowed according to the configuration of the provided {@link LiquibaseBundle}.
     *
     * @param liquibaseBundle the {@link LiquibaseBundle} instance containing the configuration for the allowed packages
     * @param clazz           the class to check
     * @return {@code true} if the class is allowed, {@code false} otherwise
     * @throws ClassNotFoundException if the class is not found
     */
    private static boolean isClassAllowed(LiquibaseBundle liquibaseBundle, Class clazz) {
        if (liquibaseBundle.allowedAllPackages()) {
            return true;
        }
        for (String allowedPackage : liquibaseBundle.getAllowedPackages()) {
            Package pkg = clazz.getPackage();
            if (pkg != null) {
                String pkgName = pkg.getName();
                if (allowedPackage.equals(pkgName) || allowedPackage.startsWith(pkgName + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

}
