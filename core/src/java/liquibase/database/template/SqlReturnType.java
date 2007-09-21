package liquibase.database.template;

import java.sql.SQLException;
import java.sql.CallableStatement;

/**
 * Interface to be implemented for retrieving values for more complex database-specific
 * types not supported by the standard <code>CallableStatement.getObject</code> method.
 *
 * <p>Implementations perform the actual work of getting the actual values. They must
 * implement the callback method <code>getTypeValue</code> which can throw SQLExceptions
 * that will be caught and translated by the calling code. This callback method has
 * access to the underlying Connection via the given CallableStatement object, if that
 * should be needed to create any database-specific objects.
 *
 * @author Thomas Risberg
 * @since 1.1
 * @see java.sql.Types
 * @see java.sql.CallableStatement#getObject
 */
public interface SqlReturnType {

	/**
	 * Constant that indicates an unknown (or unspecified) SQL type.
	 * Passed into setTypeValue if the original operation method does
	 * not specify a SQL type.
	 * @see java.sql.Types
	 */
	int TYPE_UNKNOWN = Integer.MIN_VALUE;


	/**
	 * Get the type value from the specific object.
	 * @param cs the CallableStatement to operate on
	 * @param paramIndex the index of the parameter for which we need to set the value
	 * @param sqlType SQL type of the parameter we are setting
	 * @param typeName the type name of the parameter
	 * @return the target value
	 * @throws java.sql.SQLException if a SQLException is encountered setting parameter values
	 * (that is, there's no need to catch SQLException)
	 * @see java.sql.Types
	 * @see java.sql.CallableStatement#getObject
	 */
	Object getTypeValue(CallableStatement cs, int paramIndex, int sqlType, String typeName)
			throws SQLException;

}
