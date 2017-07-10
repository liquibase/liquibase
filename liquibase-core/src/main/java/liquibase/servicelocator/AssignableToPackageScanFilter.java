package liquibase.servicelocator;

import java.util.HashSet;
import java.util.Set;

/**
 * Package scan filter for testing if a given class is assignable to another class.
 */
public class AssignableToPackageScanFilter implements PackageScanFilter {
    private final Set<Class<?>> parents = new HashSet<>();

    public AssignableToPackageScanFilter() {
    }

    public AssignableToPackageScanFilter(Class<?> parentType) {
        parents.add(parentType);
    }

    public AssignableToPackageScanFilter(Set<Class<?>> parents) {
        this.parents.addAll(parents);
    }

    public void addParentType(Class<?> parentType) {
        parents.add(parentType);
    }

    @Override
    public boolean matches(Class<?> type) {
        if ((parents != null) && !parents.isEmpty()) {
            for (Class<?> parent : parents) {
                if (parent.isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Class<?> parent : parents) {
            sb.append(parent.getSimpleName()).append(", ");
        }
        sb.setLength((sb.length() > 0) ? (sb.length() - 2) : 0);
        return "is assignable to " + sb;
    }
}
