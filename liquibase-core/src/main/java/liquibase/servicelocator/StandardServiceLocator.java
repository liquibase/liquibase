package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.exception.ServiceNotFoundException;
import liquibase.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public class StandardServiceLocator implements ServiceLocator {

    @Override
    public <T> List<T> findInstances(Class<T> interfaceType) throws ServiceNotFoundException {
        List<T> allInstances = new ArrayList<>();

        final Logger log = Scope.getCurrentScope().getLog(getClass());
        for (T t : ServiceLoader.load(interfaceType, Scope.getCurrentScope().getClassLoader(true))) {
            log.fine("Loaded "+interfaceType.getName()+" instance "+t.getClass().getName());
            allInstances.add(t);
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
