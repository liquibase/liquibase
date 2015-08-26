package liquibase.osgi;

import java.util.Collection;
import java.util.Set;

import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Package scan resolver that works with OSGI frameworks.
 */
public class OSGiPackageScanClassResolver extends DefaultPackageScanClassResolver {

    private final Bundle bundle;

    public OSGiPackageScanClassResolver(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    protected void find(PackageScanFilter test, String packageName, Set<Class<?>> classes) {
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);

        packageName = packageName.replace('.', '/');

        Collection<String> names =
                bundleWiring.listResources(packageName, "*.class", BundleWiring.LISTRESOURCES_RECURSE);
        if (names == null) {
            return;
        }
        ClassLoader bundleClassLoader = bundleWiring.getClassLoader();
        for (String name : names) {
            String fixedName = name.substring(0, name.indexOf('.')).replace('/', '.');

            try {
                Class<?> klass = bundleClassLoader.loadClass(fixedName);
                if (test.matches(klass)) {
                    classes.add(klass);
                }
            } catch (ClassNotFoundException e) {
                log.debug("Cant load class: " + e.getMessage());
            }

        }

    }
}