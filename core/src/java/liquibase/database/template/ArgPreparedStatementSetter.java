package liquibase.database.template;

import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Simple adapter for PreparedStatementSetter that applies
 * a given array of arguments.
 *
 * @author Juergen Hoeller
 */
class ArgPreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	private final Object[] args;


	/**
	 * Create a new ArgPreparedStatementSetter for the given arguments.
	 * @param args the arguments to set
	 */
	public ArgPreparedStatementSetter(Object[] args) {
		this.args = args;
	}


	public void setValues(PreparedStatement ps) throws SQLException {
		if (this.args != null) {
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				if (arg instanceof SqlParameterValue) {
					SqlParameterValue paramValue = (SqlParameterValue) arg;
					StatementCreatorUtils.setParameterValue(ps, i + 1, paramValue, paramValue.getValue());
				}
				else {
					StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, arg);
				}
			}
		}
	}

	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

}
