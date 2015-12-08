/**
 * Licensed under same license as rest of project (Apache 2.0).
 *
 * History:
 *  - github.com/samhendley - 1/31/12 : initial implementation, tested in karaf.
 */
package liquibase.osgi;

import liquibase.servicelocator.ServiceLocator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * osgi activator that patches the ServiceLocator so it will work to pickup the "builtin"
 * implementations for all of the packages. It will not work to find liquibase "plugins" that
 * use the package scanning technique to pull classes out of other libraries.
 */
public class LiquibaseActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
//        ServiceLocator.reset();
    }
}
