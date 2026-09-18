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

    /**
     * Invalidates and drops the {@link LockService} cached for just the given database, as opposed to
     * {@link #resetAll()} which drops every database's along with this factory's JVM-wide singleton.
     * Cleanup paths belonging to a single execution should use this, so they don't discard other
     * concurrently running executions' (different database's) lock services: a service dropped while
     * its execution still holds the changelog lock reports {@link LockService#hasChangeLogLock()} as
     * false, so that execution skips its release and leaves the DATABASECHANGELOGLOCK row set.
     * <p>
     * The entry is removed rather than reset in place: unlike
     * {@link liquibase.changelog.ChangeLogHistoryServiceFactory}, every service this factory hands out
     * is built on demand by {@link #getLockService(Database)}, so nothing is lost by dropping it, and
     * keeping the map free of finished databases keeps it from growing for the life of the JVM.
     */
    public void resetDatabase(Database database) {
        LockService lockService = openLockServices.remove(database);
        if (lockService != null) {
            lockService.reset();
        }
    }

    /**
     * Resets every {@link LockService} this factory knows about and drops the JVM-wide singleton. This
     * is a full teardown; cleanup paths belonging to a single execution should use
     * {@link #resetDatabase(Database)} instead.
     */
    public void resetAll() {
        for (LockService lockService : registry) {
            lockService.reset();
        }
        // registry holds only the prototypes discovered by the service locator, never the per-database
        // instances getLockService() creates from them -- which are the ones that actually hold locks.
        // reset() is idempotent, so any overlap between the two is harmless.
        openLockServices.values().forEach(LockService::reset);
        openLockServices.clear();
        reset();
    }
}
