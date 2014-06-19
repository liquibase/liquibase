package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.SingleLineComment;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CommentStatement;

public class CommentGenerator extends AbstractSqlGenerator<CommentStatement> {

	@Override
    public Sql[] generateSql(CommentStatement comment, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new SingleLineComment(comment.getText(), options.getRuntimeEnvironment().getTargetDatabase().getLineComment())
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
