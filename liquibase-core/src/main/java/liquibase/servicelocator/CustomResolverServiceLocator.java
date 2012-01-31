/**
 * Licensed under same license as rest of project (Apache 2.0).
 *
 * History:
 *  - github.com/samhendley - 1/31/12 : initial implementation, tested in karaf.
 */
package liquibase.servicelocator;

import liquibase.resource.ResourceAccessor;

/**
 * Allows overriding of the resolver class.
 */
public class CustomResolverServiceLocator extends ServiceLocator{
    public CustomResolverServiceLocator(PackageScanClassResolver classResolver) {
        super(classResolver);
    }

    public CustomResolverServiceLocator(PackageScanClassResolver classResolver, ResourceAccessor accessor) {
        super(classResolver, accessor);
    }
}
