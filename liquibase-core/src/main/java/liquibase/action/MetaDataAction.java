package liquibase.action;

import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.structure.DatabaseObject;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class MetaDataAction implements QueryAction {

    private String method;
    private Object[] parameters;

    public MetaDataAction(String method, Object... parameters) {
        this.method = method;
        this.parameters = parameters;
    }

    @Override
    public QueryResult query(ExecutionOptions options) throws DatabaseException {
        DatabaseMetaData metaData = ((JdbcConnection) options.getRuntimeEnvironment().getTargetDatabase().getConnection()).getMetaData();

        ResultSet resultSet;
        try {
            if (method.equals("getTables")) {
                resultSet = metaData.getTables((String) parameters[0], (String)parameters[1], (String)parameters[2], (String[]) parameters[3]);
            } else {
                throw new UnexpectedLiquibaseException("Unknown metadata method: "+method);
            }
            return new QueryResult(JdbcUtils.extract(resultSet));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public String toString(ExecutionOptions options) {
        return toString();
    }

    @Override
    public String toString() {
        return "DatabaseMetaData."+method+"("+ StringUtils.join(parameters, ", ", new StringUtils.ToStringFormatter())+")";
    }

    @Override
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
