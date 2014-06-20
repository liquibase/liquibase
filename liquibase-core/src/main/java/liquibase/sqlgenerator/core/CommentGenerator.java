package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.SingleLineComment;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CommentStatement;

public class CommentGenerator extends AbstractSqlGenerator<CommentStatement> {

    @Override
    public Action[] generateActions(CommentStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return new Action[] {
                new SingleLineComment(statement.getText(), options.getRuntimeEnvironment().getTargetDatabase().getLineComment())
		};
	}

	@Override
    public ValidationErrors validate(CommentStatement comment,
                                     ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("text", comment.getText());
        return validationErrors;
	}

}
