package liquibase.database.template;

import liquibase.exception.JDBCException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback interface used by {@link JdbcTemplate}'s query methods.
 * Implementations of this interface perform the actual work of extracting
 * results from a {@link java.sql.ResultSet}, but don't need to worry
 * about exception handling. {@link java.sql.SQLException SQLExceptions}
 * will be caught and handled by the calling JdbcTemplate.
 *
 * <p>This interface is mainly used within the JDBC framework itself.
 * A {@link RowMapper} is usually a simpler choice for ResultSet processing,
 * mapping one result object per row instead of one result object for
 * the entire ResultSet.
 *
 * <p>Note: In contrast to a {@link RowCallbackHandler}, a ResultSetExtractor
 * object is typically stateless and thus reusable, as long as it doesn't
 * access stateful resources (such as output streams when streaming LOB
 * contents) or keep result state within the object.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since April 24, 2003
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see RowMapper
 * @see org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor
 */
public interface ResultSetExtractor {

	/**
	 * Implementations must implement this method to process the entire ResultSet.
	 * @param rs ResultSet to extract data from. Implementations should
	 * not close this: it will be closed by the calling JdbcTemplate.
	 * @return an arbitrary result object, or <code>null</code> if none
	 * (the extractor will typically be stateful in the latter case).
	 * @throws java.sql.SQLException if a SQLException is encountered getting column
	 * values or navigating (that is, there's no need to catch SQLException)
	 * @throws DataAccessException in case of custom exceptions
	 */
	Object extractData(ResultSet rs) throws SQLException, JDBCException;

}
