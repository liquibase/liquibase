package liquibase.lockservice;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;

/**
 * Locking service for Cassandra 1.2.0
 * 
 * @author frederikcolardyn
 * 
 */
public class LockServiceCassandra implements LockService {

	private Database database;

	private boolean hasChangeLogLock = false;

	private long changeLogLockWaitTime = 1000 * 60 * 5; // default to 5 mins
	private long changeLogLocRecheckTime = 1000 * 10; // default to every 10 seconds

	public LockServiceCassandra() {
	}

	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	public boolean supports(Database database) {
		return true;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
		this.changeLogLockWaitTime = changeLogLockWaitTime;
	}

	public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
		this.changeLogLocRecheckTime = changeLogLocRecheckTime;
	}

	public boolean hasChangeLogLock() {
		return hasChangeLogLock;
	}

	public void waitForLock() throws LockException {

		boolean locked = false;
		long timeToGiveUp = new Date().getTime() + changeLogLockWaitTime;
		while (!locked && new Date().getTime() < timeToGiveUp) {
			locked = acquireLock();
			if (!locked) {
				LogFactory.getLogger().info("Waiting for changelog lock....");
				try {
					Thread.sleep(changeLogLocRecheckTime);
				} catch (InterruptedException e) {
					;
				}
			}
		}

		if (!locked) {
			DatabaseChangeLogLock[] locks = listLocks();
			String lockedBy;
			if (locks.length > 0) {
				DatabaseChangeLogLock lock = locks[0];
				lockedBy = lock.getLockedBy() + " since " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lock.getLockGranted());
			} else {
				lockedBy = "UNKNOWN";
			}
			throw new LockException("Could not acquire change log lock.  Currently locked by " + lockedBy);
		}
	}

	public boolean acquireLock() throws LockException {
		if (hasChangeLogLock) {
			return true;
		}

		Executor executor = ExecutorService.getInstance().getExecutor(database);

		try {
			database.rollback();
			database.checkDatabaseChangeLogLockTable();

			Boolean locked = (Boolean) ExecutorService.getInstance().getExecutor(database)
					.queryForObject(new SelectFromDatabaseChangeLogLockStatement("LOCKED"), Boolean.class);

			if (locked) {
				return false;
			} else {
				executor.comment("Lock Database");
				executor.update(new LockDatabaseChangeLogStatement());
				database.commit();
				LogFactory.getLogger().info("Successfully acquired change log lock");

				hasChangeLogLock = true;

				database.setCanCacheLiquibaseTableInfo(true);
				return true;
			}
		} catch (Exception e) {
			throw new LockException(e);
		} finally {
			try {
				database.rollback();
			} catch (DatabaseException e) {
				;
			}
		}

	}

	public void releaseLock() throws LockException {
		Executor executor = ExecutorService.getInstance().getExecutor(database);
		try {
			if (database.hasDatabaseChangeLogLockTable()) {
				executor.comment("Release Database Lock");
				database.rollback();
				executor.update(new UnlockDatabaseChangeLogStatement());
				database.commit();
			} else {
				System.err.println("No changelog lock table! Can not unlock.");
			}
		} catch (Exception e) {
			throw new LockException(e);
		} finally {
			try {
				hasChangeLogLock = false;

				database.setCanCacheLiquibaseTableInfo(false);

				LogFactory.getLogger().info("Successfully released change log lock");
				database.rollback();
			} catch (DatabaseException e) {
				;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public DatabaseChangeLogLock[] listLocks() throws LockException {
		try {
			if (!database.hasDatabaseChangeLogLockTable()) {
				return new DatabaseChangeLogLock[0];
			}

			List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
			SqlStatement sqlStatement = new SelectFromDatabaseChangeLogLockStatement("ID", "LOCKED", "LOCKGRANTED", "LOCKEDBY");
			List<Map> rows = ExecutorService.getInstance().getExecutor(database).queryForList(sqlStatement);
			for (Map columnMap : rows) {
				Object lockedValue = columnMap.get("LOCKED");
				Boolean locked;
				if (lockedValue instanceof Number) {
					locked = ((Number) lockedValue).intValue() == 1;
				} else {
					locked = (Boolean) lockedValue;
				}
				if (locked != null && locked) {
					allLocks.add(new DatabaseChangeLogLock(((Number) columnMap.get("ID")).intValue(), (Date) columnMap.get("LOCKGRANTED"), (String) columnMap
							.get("LOCKEDBY")));
				}
			}
			return allLocks.toArray(new DatabaseChangeLogLock[allLocks.size()]);
		} catch (Exception e) {
			throw new LockException(e);
		}
	}

	public void forceReleaseLock() throws LockException, DatabaseException {
		database.checkDatabaseChangeLogLockTable();
		releaseLock();
	}

	public void reset() {
		hasChangeLogLock = false;
	}

}
