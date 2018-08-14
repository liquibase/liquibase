package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.exception.ServiceNotFoundException;

import java.util.List;

/**
 * Abstraction for finding and creating instances of classes.
 * {@link StandardServiceLocator} is the main implementation, but can be overridden if need be.
 *
 * The ServiceLocator to use should be accessed via {@link Scope#getServiceLocator()}
 */
public interface ServiceLocator {


    <T> List<T> findInstances(Class<T> interfaceType) throws ServiceNotFoundException;


    <T> List<Class<? extends T>> findClasses(Class<T> interfaceType) throws ServiceNotFoundException;
}
