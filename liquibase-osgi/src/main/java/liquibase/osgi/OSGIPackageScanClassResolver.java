/**
 * Licensed under same license as rest of project (Apache 2.0).
 *
 * History:
 *  - github.com/samhendley - 1/31/12 : initial implementation, tested in karaf.
 */
package liquibase.osgi;

import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;
import org.osgi.framework.Bundle;

import java.util.Enumeration;
import java.util.Set;

/**
 * Package scan resolver that works with OSGI frameworks (in theory all of them)
 */
public class OSGIPackageScanClassResolver extends DefaultPackageScanClassResolver {

    private final Bundle bundle;

    public OSGIPackageScanClassResolver(Bundle bundle){
        this.bundle = bundle;
    }

    @Override
    protected void find(PackageScanFilter test, String packageName, Set<Class<?>> classes) {
        packageName = packageName.replace('.', '/');

        Enumeration names = bundle.getEntryPaths(packageName);

        while (names != null && names.hasMoreElements()) {
            String name = (String) names.nextElement();
            if(name.endsWith("/")){
                find(test, name, classes);
            }
            else if(name.endsWith(".class")) {
                try{
                    // strip off .class and liquibase/database/Abstract.class -> liquibase.database.Abstract
                    String fixedName = name.substring(0, name.indexOf('.')).replace('/','.');

                    Class<?> klass = bundle.loadClass(fixedName);

                    if (test.matches(klass)) {
                        classes.add(klass);
                    }
                }catch(Exception cce){
                    log.debug("Cant load class: " + cce.getMessage());
                }
            }
        }

    }
}