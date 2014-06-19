package liquibase.sql;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.executor.UpdateResult;
import liquibase.structure.DatabaseObject;

import java.util.Collection;
import java.util.HashSet;

public class SingleLineComment implements Sql {

	final private String sql;
	final private String lineCommentToken;
	
	public SingleLineComment(String sql, String lineCommentToken) {
		this.sql = sql;
		this.lineCommentToken = lineCommentToken;
	}
	
	@Override
    public String getEndDelimiter() {
		return "\n";
	}

	@Override
    public String toSql() {
		return lineCommentToken + ' ' + sql;
	}

    @Override
    public String toString() {
        return toSql();
    }

    @Override
    public ExecuteResult execute(ExecutionOptions options) throws DatabaseException {
        return new ExecuteResult();
    }

    @Override
    public QueryResult query(ExecutionOptions options) throws DatabaseException {
        return new QueryResult(null);
    }

    @Override
    public UpdateResult update(ExecutionOptions options) throws DatabaseException {
        return new UpdateResult(0);
    }

    @Override
    public String toString(ExecutionOptions options) {
        return toSql();
    }
}
