/**
 * 
 */
package liquibase.database.structure;

import java.util.Set;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

/**
 * @author otaranenko
 *
 */
public class SybaseASADatabaseSnapshot extends SqlDatabaseSnapshot {

	/**
	 * 
	 */
	public SybaseASADatabaseSnapshot() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param database
	 * @throws JDBCException
	 */
	public SybaseASADatabaseSnapshot(Database database) throws JDBCException {
		super(database);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param database
	 * @param schema
	 * @throws JDBCException
	 */
	public SybaseASADatabaseSnapshot(Database database, String schema)
			throws JDBCException {
		super(database, schema);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param database
	 * @param statusListeners
	 * @throws JDBCException
	 */
	public SybaseASADatabaseSnapshot(Database database,
			Set<DiffStatusListener> statusListeners) throws JDBCException {
		super(database, statusListeners);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param database
	 * @param statusListeners
	 * @param requestedSchema
	 * @throws JDBCException
	 */
	public SybaseASADatabaseSnapshot(Database database,
			Set<DiffStatusListener> statusListeners, String requestedSchema)
			throws JDBCException {
		super(database, statusListeners, requestedSchema);
		// TODO Auto-generated constructor stub
	}

}
