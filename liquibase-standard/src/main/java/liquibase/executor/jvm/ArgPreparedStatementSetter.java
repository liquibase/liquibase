package liquibase.executor.jvm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple adapter for PreparedStatementSetter that applies
 * a given array of arguments.
 *
 * @author Spring Framework
 */
class ArgPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;


    /**
     * Create a new ArgPreparedStatementSetter for the given arguments.
     *
     * @param args the arguments to set
     */
    public ArgPreparedStatementSetter(Object[] args) {
        this.args = args;
    }


    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        if (this.args != null) {
            for (int i = 0; i < this.args.length; i++) {
                Object arg = this.args[i];
                if (arg instanceof SqlParameterValue) {
                    SqlParameterValue paramValue = (SqlParameterValue) arg;
                    StatementCreatorUtils.setParameterValue(ps, i + 1, paramValue, paramValue.getValue());
                } else {
                    StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, arg);
                }
            }
        }
    }


}
