package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author John Sanda
 */
public class LockServiceFactory {

	private static LockServiceFactory instance;

	private List<LockService> registry = new ArrayList<>();

	private Map<Database, LockService> openLockServices = new ConcurrentHashMap<>();

	public static synchronized LockServiceFactory getInstance() {
		if (instance == null) {
			instance = new LockServiceFactory();
		}
		return instance;
	}

    /**
     * Set the instance used by this singleton. Used primarily for testing.
     */
    public static void setInstance(LockServiceFactory lockServiceFactory) {
        LockServiceFactory.instance = lockServiceFactory;
    }


    public static synchronized void reset() {
        instance = null;
    }

    private LockServiceFactory() {
		Class<? extends LockService>[] classes;
		try {
			classes = ServiceLocator.getInstance().findClasses(LockService.class);

			for (Class<? extends LockService> clazz : classes) {
				register(clazz.getConstructor().newInstance());
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void register(LockService lockService) {
		registry.add(0, lockService);
	}

	public LockService getLockService(Database database) {
		if (!openLockServices.containsKey(database)) {
			SortedSet<LockService> foundServices = new TreeSet<>(new Comparator<LockService>() {
                @Override
                public int compare(LockService o1, LockService o2) {
                    return -1 * Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
                }
            });

			for (LockService lockService : registry) {
				if (lockService.supports(database)) {
					foundServices.add(lockService);
				}
			}

			if (foundServices.isEmpty()) {
				throw new UnexpectedLiquibaseException("Cannot find LockService for " + database.getShortName());
			}

			try {
				LockService lockService = foundServices.iterator().next().getClass().newInstance();
				lockService.setDatabase(database);
				openLockServices.put(database, lockService);
			} catch (Exception e) {
				throw new UnexpectedLiquibaseException(e);
			}
		}
		return openLockServices.get(database);

	}

	public synchronized void resetAll() {
		for (LockService lockService : registry) {
			lockService.reset();
		}
		instance = null;
	}

}
