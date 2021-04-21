package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LockServiceFactory extends AbstractPluginFactory<LockService> {

	private final Map<Database, LockService> openLockServices = new ConcurrentHashMap<>();

	protected LockServiceFactory() {
	}

	@Override
	protected Class<LockService> getPluginClass() {
		return LockService.class;
	}

	@Override
	protected int getPriority(LockService obj, Object... args) {
		final Database database = (Database) args[0];
		if (obj.supports(database)) {
			return obj.getPriority();
        }
		return Plugin.PRIORITY_NOT_APPLICABLE;
	  }

	public LockService getLockService(Database database) throws DatabaseException {
        if (!openLockServices.containsKey(database)) {
			LockService lockService = getPlugin(database);

			if (lockService == null) {
                throw new UnexpectedLiquibaseException("Cannot find LockService for " + database.getShortName());
			      }

			lockService = (LockService) lockService.clone();
			lockService.setDatabase(database);
			lockService.init();

			openLockServices.put(database, lockService);
        }
        return openLockServices.get(database);
    }

    public synchronized void resetAll() {
		for (LockService lockService : openLockServices.values()) {
            lockService.reset();
        }
    }
}
