package liquibase.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import liquibase.resource.ClassLoaderResourceAccessor;

public class OSGiResourceAccessor extends ClassLoaderResourceAccessor {

    private final Bundle bundle;

    public OSGiResourceAccessor(Bundle bundle) {
        super(bundle.adapt(BundleWiring.class).getClassLoader());
        this.bundle = bundle;
    }
    
    public Bundle getBundle() {
        return bundle;
    }
}
