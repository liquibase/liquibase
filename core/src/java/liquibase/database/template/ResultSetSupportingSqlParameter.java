package liquibase.database.template;

/**
 * Common base class for ResultSet-supporting SqlParameters like
 * {@link SqlOutParameter} and {@link SqlReturnResultSet}.
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 */
public class ResultSetSupportingSqlParameter extends SqlParameter {

	private ResultSetExtractor resultSetExtractor;

	private RowCallbackHandler rowCallbackHandler;

	private RowMapper rowMapper;


	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType) {
		super(name, sqlType);
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, String typeName) {
		super(name, sqlType, typeName);
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rse ResultSetExtractor to use for parsing the ResultSet
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, ResultSetExtractor rse) {
		super(name, sqlType);
		this.resultSetExtractor = rse;
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rch RowCallbackHandler to use for parsing the ResultSet
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, RowCallbackHandler rch) {
		super(name, sqlType);
		this.rowCallbackHandler = rch;
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rm RowMapper to use for parsing the ResultSet
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, RowMapper rm) {
		super(name, sqlType);
		this.rowMapper = rm;
	}


	/**
	 * Does this parameter support a ResultSet, i.e. does it hold a
	 * ResultSetExtractor, RowCallbackHandler or RowMapper?
	 */
	public boolean isResultSetSupported() {
		return (this.resultSetExtractor != null || this.rowCallbackHandler != null || this.rowMapper != null);
	}

	/**
	 * Return the ResultSetExtractor held by this parameter, if any.
	 */
	public ResultSetExtractor getResultSetExtractor() {
		return resultSetExtractor;
	}

	/**
	 * Return the RowCallbackHandler held by this parameter, if any.
	 */
	public RowCallbackHandler getRowCallbackHandler() {
		return this.rowCallbackHandler;
	}

	/**
	 * Return the RowMapper held by this parameter, if any.
	 */
	public RowMapper getRowMapper() {
		return this.rowMapper;
	}


	/**
	 * <p>This implementation always returns <code>false</code>.
	 */
	public boolean isInputValueProvided() {
		return false;
	}

}
