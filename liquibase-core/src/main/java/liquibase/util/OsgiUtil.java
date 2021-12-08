package liquibase.util;

import java.util.List;
import java.util.stream.Collectors;
import liquibase.osgi.Activator;
import liquibase.osgi.Activator.LiquibaseBundle;

public final class OsgiUtil {

    private OsgiUtil() {
    }

    /**
     * try to load a class under OSGI environment. It will try to load the class
     * from all liquibase bundles registered via
     * {@link Activator Activator}
     *
     * @param <T>
     * @param className name of class
     * @return
     * @throws ClassNotFoundException
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
     *
     * @param clazz
     * @return true is a class is allowed
     * @throws java.lang.ClassNotFoundException
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
