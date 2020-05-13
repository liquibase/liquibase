package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.exception.ServiceNotFoundException;
import liquibase.logging.Logger;

import java.util.*;

public class StandardServiceLocator implements ServiceLocator {

    @Override
    public <T> List<T> findInstances(Class<T> interfaceType) throws ServiceNotFoundException {
        List<T> allInstances = new ArrayList<>();

        final Logger log = Scope.getCurrentScope().getLog(getClass());
        final Iterator<T> services = ServiceLoader.load(interfaceType, Scope.getCurrentScope().getClassLoader(true)).iterator();
        while (services.hasNext()) {
            try {
                final T service = services.next();
                log.fine("Loaded "+interfaceType.getName()+" instance "+service.getClass().getName());
                allInstances.add(service);
            } catch (Throwable e) {
                log.info("Cannot load service: "+e.getMessage());
            }
        }

        return Collections.unmodifiableList(allInstances);

    }

    @Override
    public <T> List<Class<? extends T>> findClasses(Class<T> interfaceType) throws ServiceNotFoundException {
        List<Class<T>> allInstances = new ArrayList<>();

        for (T t : findInstances(interfaceType)) {
            allInstances.add((Class<T>) t.getClass());
        }

        return Collections.unmodifiableList(allInstances);

    }
}
