package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.SingleLineComment;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.CommentStatement;

public class CommentGenerator extends AbstractSqlGenerator<CommentStatement> {

    @Override
    public Action[] generateActions(CommentStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new Action[] {
                new SingleLineComment(statement.getText(), env.getTargetDatabase().getLineComment())
		};
	}

	@Override
    public ValidationErrors validate(CommentStatement comment,
                                     ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("text", comment.getText());
        return validationErrors;
	}

}
