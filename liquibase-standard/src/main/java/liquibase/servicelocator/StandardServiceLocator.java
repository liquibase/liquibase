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
        List<T> allInstances = new ArrayList<>();

        final Logger log = Scope.getCurrentScope().getLog(getClass());
        final Iterator<T> services = ServiceLoader.load(interfaceType, Scope.getCurrentScope().getClassLoader(true)).iterator();
        while (services.hasNext()) {
            try {
                final T service = services.next();
                log.fine("Loaded "+interfaceType.getName()+" instance "+service.getClass().getName());
                allInstances.add(service);
            } catch (Throwable e) {
                new ServiceLoadExceptionHandler().handleException(e);
            }
        }

        return Collections.unmodifiableList(allInstances);

    }

    /**
     * Exception handler for when a service cannot be loaded. Created as an inner class so logs can be suppressed if desired.
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
