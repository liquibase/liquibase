package liquibase.lockservice;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author John Sanda
 */
public class LockServiceFactory {

    private static LockServiceFactory instance;

	  private final Deque<LockService> registry = new ConcurrentLinkedDeque<>();

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
        registry.addFirst(lockService);
    }

    private LockService instantiate(Database database) {
        LockService example = registry
                .stream()
                .filter(ls -> ls.supports(database))
                .min(PrioritizedService.COMPARATOR)
                .orElseThrow(() -> new UnexpectedLiquibaseException(
                        "Cannot find LockService for " + database.getShortName()
                ));

        try {
            LockService lockService = example.getClass().getConstructor().newInstance();
            lockService.setDatabase(database);
            return lockService;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public LockService getLockService(Database database) {
        return openLockServices.computeIfAbsent(database, this::instantiate);
    }

    public void resetAll() {
        synchronized (LockServiceFactory.class) {
            for (LockService lockService : registry) {
                lockService.reset();
            }
            reset();
        }
    }
}
