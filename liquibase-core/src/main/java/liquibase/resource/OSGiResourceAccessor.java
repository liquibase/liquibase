package liquibase.resource;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

public class OSGiResourceAccessor extends ClassLoaderResourceAccessor {

    public OSGiResourceAccessor(Bundle bundle) {
        super(bundle.adapt(BundleWiring.class).getClassLoader());
    }
}
