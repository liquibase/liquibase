package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.exception.ServiceNotFoundException;
import liquibase.logging.Logger;

import java.util.*;

public class StandardServiceLocator implements ServiceLocator {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public <T> List<T> findInstances(Class<T> interfaceType) throws ServiceNotFoundException {
        final List<T> allInstances = new ArrayList<>();
        final Set<String> classNameSet = new HashSet<>();
        final Logger log = Scope.getCurrentScope().getLog(getClass());
        ClassLoader classLoader = Scope.getCurrentScope().getClassLoader(true);
        findInstances(interfaceType, allInstances, log, classLoader, classNameSet);

        if (!classLoader.equals(interfaceType.getClassLoader())) {
            findInstances(interfaceType, allInstances, log, interfaceType.getClassLoader(), classNameSet);
        }

        return Collections.unmodifiableList(allInstances);
    }

    private static <T> void findInstances(Class<T> interfaceType, List<T> allInstances, Logger log, ClassLoader classLoader,
                                          Set<String> classNameSet) {
        final Iterator<T> services = ServiceLoader.load(interfaceType, classLoader).iterator();

        while (services.hasNext()) {

            try {
                final T service = services.next();
                String className = service.getClass().getName();

                if (!classNameSet.contains(className)) {
                    log.fine("Loaded "+ interfaceType.getName()+" instance "+ className);
                    classNameSet.add(className);
                    allInstances.add(service);
                }

            } catch (Throwable e) {
                log.info("Cannot load service: "+e.getMessage());
                log.fine(e.getMessage(), e);
            }

        }

    }
}
