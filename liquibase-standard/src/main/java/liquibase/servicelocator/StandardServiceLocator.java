package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.exception.ServiceNotFoundException;
import liquibase.logging.Logger;
import liquibase.util.SystemUtil;

import java.util.*;
import java.util.logging.Level;

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

        // in some classloader setups, the classloader that loaded the interface may not be the same as the one from current scope
        // ie: a classloader from a child module may load the interface, but the service implementations are in the parent module
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
                    log.fine(String.format("Loaded %s instance %s", interfaceType.getName(), className));
                    classNameSet.add(className);
                    allInstances.add(service);
                }

            } catch (Throwable e) {
                new ServiceLoadExceptionHandler().handleException(e);
            }

        }

    }

    /**
     * Exception handler used when a service cannot be loaded. Created as an inner class so logs can be suppressed if desired.
     */
    static class ServiceLoadExceptionHandler {
        void handleException(Throwable e) {
            Level level = Level.INFO;
            if (e instanceof UnsupportedClassVersionError && !SystemUtil.isAtLeastJava11() && e.getMessage().contains("BigQuery")) {
                level = Level.FINE;
            }
            Scope.getCurrentScope().getLog(getClass()).log(level,"Cannot load service: " + e.getMessage(), e);
        }
    }
}
