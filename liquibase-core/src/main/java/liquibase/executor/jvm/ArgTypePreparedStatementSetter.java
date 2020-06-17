package liquibase.executor.jvm;

import liquibase.exception.DatabaseException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

/**
 * Simple adapter for PreparedStatementSetter that applies
 * given arrays of arguments and JDBC argument types.
 *
 * @author Spring Framework
 */
class ArgTypePreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    private final int[] argTypes;


    /**
     * Create a new ArgTypePreparedStatementSetter for the given arguments.
     *
     * @param args     the arguments to set
     * @param argTypes the corresponding SQL types of the arguments
     */
    public ArgTypePreparedStatementSetter(Object[] args, int[] argTypes) throws DatabaseException {
        if (((args != null) && (argTypes == null)) || ((args == null) && (argTypes != null)) || ((args != null) &&
            (args.length != argTypes.length))) {
            throw new DatabaseException("args and argTypes parameters must match");
        }
        this.args = args;
        this.argTypes = argTypes;
    }


    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        int argIndx = 1;
        if (this.args != null) {
            for (int i = 0; i < this.args.length; i++) {
                Object arg = this.args[i];
                if ((arg instanceof Collection) && (this.argTypes[i] != Types.ARRAY)) {
                    Collection entries = (Collection) arg;
                    for (Object entry : entries) {
                        StatementCreatorUtils.setParameterValue(ps, argIndx++, this.argTypes[i], entry);
                    }
                } else {
                    StatementCreatorUtils.setParameterValue(ps, argIndx++, this.argTypes[i], arg);
                }
            }
        }
    }


}
