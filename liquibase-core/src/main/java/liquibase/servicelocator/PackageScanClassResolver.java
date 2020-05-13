package liquibase.servicelocator;

import java.util.Set;

/**
 * A resolver that can find resources based on package scanning.
 */
public interface PackageScanClassResolver {

    /**
     * Sets the ClassLoader instances that should be used when scanning for
     * classes. If none is set then the context classloader will be used.
     *
     * @param classLoaders loaders to use when scanning for classes
     */
    void setClassLoaders(Set<ClassLoader> classLoaders);

    /**
     * Gets the ClassLoader instances that should be used when scanning for classes.
     *
     * @return the class loaders to use
     */
    Set<ClassLoader> getClassLoaders();

    /**
     * Adds the class loader to the existing loaders
     *
     * @param classLoader the loader to add
     */
    void addClassLoader(ClassLoader classLoader);

    /**
     * Attempts to discover classes that are assignable to the type provided. In
     * the case that an interface is provided this method will collect
     * implementations. In the case of a non-interface class, subclasses will be
     * collected.
     *
     * @param parent       the class of interface to find subclasses or implementations of
     * @param packageNames one or more package names to scan (including subpackages) for classes
     * @return the classes found, returns an empty set if none found
     */
    Set<Class<?>> findImplementations(Class<?> parent, String... packageNames);

    /**
     * Attempts to discover classes filter by the provided filter
     *
     * @param filter  filter to filter desired classes.
     * @param packageNames one or more package names to scan (including subpackages) for classes
     * @return the classes found, returns an empty set if none found
     */
    Set<Class<?>> findByFilter(PackageScanFilter filter, String... packageNames);

    /**
     * Add a filter that will be applied to all scan operations
     *
     * @param filter filter to filter desired classes in all scan operations
     */
    void addFilter(PackageScanFilter filter);

    /**
     * Removes the filter
     *
     * @param filter filter to filter desired classes in all scan operations
     */
    void removeFilter(PackageScanFilter filter);
}
