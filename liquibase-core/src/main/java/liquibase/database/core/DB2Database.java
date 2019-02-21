package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtils;

public class DB2Database extends AbstractDb2Database {

	@Override
	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
		return conn.getDatabaseProductName().startsWith("DB2")
				&& !StringUtils.startsWith(conn.getDatabaseProductVersion(), "DSN");
	}

	@Override
	public String getShortName() {
		return "db2";
	}

	/**
	 * boolean data type column are allowed for versions >= 11.1.1.1
	 * @return
	 */
	public boolean supportsBooleanDataType() {
		if (getConnection() == null)
			return false; /// assume not;
		try {

			final Integer fixPack = getDb2FixPack();

			if (fixPack == null)
				throw new DatabaseException("Error getting fix pack number");

			return getDatabaseMajorVersion() > 11
					|| getDatabaseMajorVersion() == 11 && getDatabaseMinorVersion() >= 1 && fixPack.intValue() >= 1;

		} catch (final DatabaseException e) {
			return false; // assume not
		}
	}

	private Integer getDb2FixPack() {
		if (getConnection() == null || getConnection() instanceof OfflineConnection)
			return null;
		try {
			return ExecutorService.getInstance().getExecutor(this).queryForObject(
					new RawSqlStatement("SELECT fixpack_num FROM TABLE (sysproc.env_get_inst_info()) as INSTANCEINFO"),
					Integer.class);
		} catch (final Exception e) {
			LogService.getLog(getClass()).info(LogType.LOG, "Error getting fix pack number", e);
		}
		return null;
	}

}
