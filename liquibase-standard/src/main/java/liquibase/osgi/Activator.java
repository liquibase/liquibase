package liquibase.osgi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import liquibase.osgi.Activator.LiquibaseBundle;
import lombok.Getter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

public class Activator implements BundleActivator, BundleTrackerCustomizer<LiquibaseBundle> {

    private static final String LIQUIBASE_CUSTOM_CHANGE_WRAPPER_PACKAGES = "Liquibase-Custom-Change-Packages";
    private BundleTracker<LiquibaseBundle> bundleTracker;
    private static final List<LiquibaseBundle> liquibaseBundles = new CopyOnWriteArrayList<>();

    @Override
    public void start(final BundleContext bc) throws Exception {
        ContainerChecker.osgiPlatform();
        bundleTracker = new BundleTracker<>(bc, Bundle.ACTIVE, this);
        bundleTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        bundleTracker.close();
        liquibaseBundles.clear();
    }

    public static List<LiquibaseBundle> getLiquibaseBundles() {
        return Collections.unmodifiableList(liquibaseBundles);
    }

    @Override
    public LiquibaseBundle addingBundle(Bundle bundle, BundleEvent event) {
        if (bundle.getBundleId() == 0) {
            return null;
        }
        String customWrapperPackages = bundle.getHeaders().get(LIQUIBASE_CUSTOM_CHANGE_WRAPPER_PACKAGES);
        if (customWrapperPackages != null) {
            LiquibaseBundle lb = new LiquibaseBundle(bundle, customWrapperPackages);
            liquibaseBundles.add(lb);
            return lb;
        }
        return null;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, LiquibaseBundle liquibaseBundle) {
        // nothing to do
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, LiquibaseBundle liquibaseBundle) {
        if (liquibaseBundle != null) {
            liquibaseBundles.remove(liquibaseBundle);
        }
    }

    @Getter
    public static class LiquibaseBundle {

        private final Bundle bundle;
        private final List<String> allowedPackages;

        public LiquibaseBundle(Bundle bundle, String allowedPackages) {
            if (bundle == null) {
                throw new IllegalArgumentException("bundle cannot be empty");
            }
            if (allowedPackages == null || allowedPackages.isEmpty()) {
                throw new IllegalArgumentException("packages cannot be empty");
            }
            this.bundle = bundle;
            this.allowedPackages = Collections.unmodifiableList(Arrays.asList(allowedPackages.split(",")));
        }

        public boolean allowedAllPackages() {
            return allowedPackages.size() == 1
                    && "*".equals(allowedPackages.get(0));
        }

    }

    /**
     * @deprecated use {@link ContainerChecker}
     */
    @Deprecated
    public static class OSGIContainerChecker extends ContainerChecker {

    }

}
