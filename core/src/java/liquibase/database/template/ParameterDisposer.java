package liquibase.database.template;

/**
 * Interface to be implemented by objects that can close resources
 * allocated by parameters like SqlLobValues.
 *
 * <p>Typically implemented by PreparedStatementCreators and
 * PreparedStatementSetters that support DisposableSqlTypeValue
 * objects (e.g. SqlLobValue) as parameters.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see PreparedStatementCreator
 * @see PreparedStatementSetter
 * @see DisposableSqlTypeValue
 * @see org.springframework.jdbc.core.support.SqlLobValue
 */
public interface ParameterDisposer {

	/**
	 * Close the resources allocated by parameters that the implementing
	 * object holds, for example in case of a DisposableSqlTypeValue
	 * (like a SqlLobValue).
	 * @see DisposableSqlTypeValue#cleanup
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup
	 */
	public void cleanupParameters();

}
