package liquibase.changelog;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChangeLogHistoryServiceFactory {

    private static ChangeLogHistoryServiceFactory instance;

    private List<ChangeLogHistoryService> registry = new ArrayList<>();

    private Map<Database, ChangeLogHistoryService> services = new ConcurrentHashMap<>();

    public static synchronized ChangeLogHistoryServiceFactory getInstance() {
        if (instance == null) {
            instance = new ChangeLogHistoryServiceFactory();
        }
        return instance;
    }

    /**
     * Set the instance used by this singleton. Used primarily for testing.
     */
    public static synchronized void setInstance(ChangeLogHistoryServiceFactory changeLogHistoryServiceFactory) {
        ChangeLogHistoryServiceFactory.instance = changeLogHistoryServiceFactory;
    }


    public static synchronized void reset() {
        instance = null;
    }

    private ChangeLogHistoryServiceFactory() {
        try {
            for (ChangeLogHistoryService service : Scope.getCurrentScope().getServiceLocator().findInstances(ChangeLogHistoryService.class)) {
                register(service);
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void register(ChangeLogHistoryService changeLogHistoryService) {
        registry.add(0, changeLogHistoryService);
    }

    public ChangeLogHistoryService getChangeLogService(Database database) {
            if (services.containsKey(database)) {
                return services.get(database);
            }
            SortedSet<ChangeLogHistoryService> foundServices = new TreeSet<>(new Comparator<ChangeLogHistoryService>() {
                @Override
                public int compare(ChangeLogHistoryService o1, ChangeLogHistoryService o2) {
                    return -1 * Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
                }
            });

            for (ChangeLogHistoryService service : registry) {
                if (service.supports(database)) {
                    foundServices.add(service);
                }
            }

            if (foundServices.isEmpty()) {
                throw new UnexpectedLiquibaseException("Cannot find ChangeLogHistoryService for " +
                    database.getShortName());
            }

            try {
                ChangeLogHistoryService exampleService = foundServices.iterator().next();
                Class<? extends ChangeLogHistoryService> aClass = exampleService.getClass();
                ChangeLogHistoryService service;
                try {
                    aClass.getConstructor();
                    service = aClass.newInstance();
                    service.setDatabase(database);
                } catch (NoSuchMethodException e) {
                    // must have been manually added to the registry and so already configured.
                    service = exampleService;
                }

                services.put(database, service);
                return service;
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
    }

    public synchronized void resetAll() {
        for (ChangeLogHistoryService changeLogHistoryService : registry) {
            changeLogHistoryService.reset();
        }
        instance = null;
    }

}

