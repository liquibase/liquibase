package liquibase.lockservice;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author John Sanda
 */
public class LockServiceFactory {

    private static LockServiceFactory instance;

	  private final List<LockService> registry = new ArrayList<>();

	  private final Map<Database, LockService> openLockServices = new ConcurrentHashMap<>();

	  public static synchronized LockServiceFactory getInstance() {
        if (instance == null) {
			      instance = new LockServiceFactory();
		    }
		    return instance;
    }

    /**
     * Set the instance used by this singleton. Used primarily for testing.
     */
    public static synchronized void setInstance(LockServiceFactory lockServiceFactory) {
        LockServiceFactory.instance = lockServiceFactory;
    }

    public static synchronized void reset() {
        instance = null;
    }

    private LockServiceFactory() {
		    try {
			      for (LockService lockService : Scope.getCurrentScope().getServiceLocator().findInstances(LockService.class)) {
				        register(lockService);
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
			      SortedSet<LockService> foundServices = new TreeSet<>((o1, o2) -> -1 * Integer.compare(o1.getPriority(), o2.getPriority()));

            for (LockService lockService : registry) {
                if (lockService.supports(database)) {
					          foundServices.add(lockService);
				        }
            }

            if (foundServices.isEmpty()) {
                throw new UnexpectedLiquibaseException("Cannot find LockService for " + database.getShortName());
			      }

            try {
                LockService lockService = foundServices.iterator().next().getClass().getConstructor().newInstance();
                lockService.setDatabase(database);
                openLockServices.put(database, lockService);
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return openLockServices.get(database);
    }

    public void resetAll() {
        for (LockService lockService : registry) {
            lockService.reset();
        }
        reset();
    }
}
