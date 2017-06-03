package liquibase.database.jvm;

import java.sql.Connection;

import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;

/**
 * A SybaseASA specific Delegate that removes the calls 
 * to nativeSQL because driver issues.
 * 
 * @author <a href="mailto:andreas.pohl@mateurope.com">Andreas Pohl</a>
 *
 */
public class SybaseASAConnection extends JdbcConnection {

	public SybaseASAConnection(Connection connection) {
		super(connection);
	}

	@Override
	public String nativeSQL(String sql) throws DatabaseException {
    	return sql;
    }

}
