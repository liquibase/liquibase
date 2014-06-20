package liquibase.action.core;

import liquibase.action.*;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.executor.UpdateResult;

/**
 * Action that is a no-op for execute, query and update operations, but is able to provide a message in the {@link liquibase.action.Action#describe()} method.
 */
public class SingleLineComment implements QueryAction, ExecuteAction, UpdateAction {

	final private String message;
	final private String lineCommentToken;
	
	public SingleLineComment(String message, String lineCommentToken) {
		this.message = message;
		this.lineCommentToken = lineCommentToken;
	}
	
    @Override
    public String toString() {
        return describe();
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
    public String describe() {
        String message = this.message;
        String commentToken = this.lineCommentToken;

        if (commentToken == null) {
            commentToken = "";
        } else {
            if (message == null) {
                return commentToken;
            }
            commentToken = commentToken + " ";
        }

        if (message == null) {
            message = "";
        }

        return commentToken + message;
    }
}
