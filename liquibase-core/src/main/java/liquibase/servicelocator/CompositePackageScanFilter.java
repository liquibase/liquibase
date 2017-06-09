package liquibase.servicelocator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <code>CompositePackageScanFilter</code> allows multiple
 * {@link PackageScanFilter}s to be composed into a single filter. For a
 * {@link Class} to match a {@link CompositePackageScanFilter} it must match
 * each of the filters the composite contains
 */
public class CompositePackageScanFilter implements PackageScanFilter {
    private Set<PackageScanFilter> filters;

    public CompositePackageScanFilter() {
        filters = new LinkedHashSet<>();
    }

    public CompositePackageScanFilter(Set<PackageScanFilter> filters) {
        this.filters = new LinkedHashSet<>(filters);
    }

    public void addFilter(PackageScanFilter filter) {
        filters.add(filter);
    }

    @Override
    public boolean matches(Class<?> type) {
        for (PackageScanFilter filter : filters) {
            if (!filter.matches(type)) {
                return false;
            }
        }
        return true;
    }
}
