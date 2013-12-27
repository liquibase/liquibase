package liquibase.changelog;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChangeLogServiceFactory {

    private static ChangeLogServiceFactory instance;

    private List<ChangeLogService> registry = new ArrayList<ChangeLogService>();

    private Map<Database, ChangeLogService> services = new ConcurrentHashMap<Database, ChangeLogService>();

    public static synchronized ChangeLogServiceFactory getInstance() {
        if (instance == null) {
            instance = new ChangeLogServiceFactory();
        }
        return instance;
    }

    /**
     * Set the instance used by this singleton. Used primarily for testing.
     */
    public static void setInstance(ChangeLogServiceFactory changeLogServiceFactory) {
        ChangeLogServiceFactory.instance = changeLogServiceFactory;
    }


    public static void reset() {
        instance = null;
    }

    private ChangeLogServiceFactory() {
        Class<? extends ChangeLogService>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(ChangeLogService.class);

            for (Class<? extends ChangeLogService> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void register(ChangeLogService changeLogService) {
        registry.add(0, changeLogService);
    }

    public ChangeLogService getChangeLogService(Database database) {
            if (services.containsKey(database)) {
                return services.get(database);
            }
            SortedSet<ChangeLogService> foundServices = new TreeSet<ChangeLogService>(new Comparator<ChangeLogService>() {
                @Override
                public int compare(ChangeLogService o1, ChangeLogService o2) {
                    return -1 * new Integer(o1.getPriority()).compareTo(o2.getPriority());
                }
            });

            for (ChangeLogService service : registry) {
                if (service.supports(database)) {
                    foundServices.add(service);
                }
            }

            if (foundServices.size() == 0) {
                throw new UnexpectedLiquibaseException("Cannot find ChangeLogService for " + database.getShortName());
            }

            try {
                ChangeLogService service = foundServices.iterator().next().getClass().newInstance();
                service.setDatabase(database);

                services.put(database, service);
                return service;
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
    }

    public void resetAll() {
        for (ChangeLogService changeLogService : registry) {
            changeLogService.reset();
        }
        instance = null;
    }

}

