package liquibase.lockservice;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

/**
 * @author John Sanda
 */
public class LockServiceFactory {

    private static class LockServiceComparator implements Comparator<LockService> {
        public int compare(LockService o1, LockService o2) {
            return -1 * (new Integer(o1.getPriority()).compareTo(o2.getPriority()));
        }
    }

    private static LockServiceFactory instance;

    private Map<Database, SortedSet<LockService>> registry =
        new ConcurrentHashMap<Database, SortedSet<LockService>>();

    private LockServiceComparator comparator = new LockServiceComparator();

    public static LockServiceFactory getInstance() {
        if (instance == null) {
            instance = new LockServiceFactory();
        }
        return instance;
    }

    private LockServiceFactory() {
        Class<? extends LockService>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(LockService.class);

            for (Class<? extends LockService> clazz : classes) {
                register(clazz);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void register(Class<? extends LockService> clazz) {
        DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
        List<Database> databases = databaseFactory.getImplementedDatabases();

        for (Database database : databases) {
            if (!registry.containsKey(database)) {
                registry.put(database, new TreeSet<LockService>(comparator));
            }
            try {
                LockService lockService = clazz.newInstance();
                if (lockService.supports(database)) {
                    SortedSet<LockService> set = registry.get(database);
                    set.add(lockService);
                }
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
    }

    public LockService getLockService(Database database) {
        SortedSet<LockService> set = registry.get(database);

        if (set == null) {
            return null;
        }

        return set.iterator().next();
    }

    public void resetAll() {
        for (Database database : registry.keySet()) {
            for (LockService lockService : registry.get(database)) {
                lockService.reset();
            }
        }
        instance = null;
    }

}
