package liquibase.resource;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import java.nio.file.Path;

public class OSGiResourceAccessor extends ClassLoaderResourceAccessor {

    public OSGiResourceAccessor(Bundle... bundles) {
        for (Bundle bundle : bundles) {
            try {
                for (Path path : findRootPaths(bundle.adapt(BundleWiring.class).getClassLoader())) {
                    addRootPath(path);
                }
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
    }
}
