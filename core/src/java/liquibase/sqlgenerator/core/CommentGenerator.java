package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.SingleLineComment;
import liquibase.sql.Sql;
import liquibase.statement.CommentStatement;
import liquibase.sqlgenerator.SqlGenerator;

public class CommentGenerator implements SqlGenerator<CommentStatement> {

	public Sql[] generateSql(CommentStatement comment, Database database) {
        return new Sql[] {
                new SingleLineComment(comment.getText(), database.getLineComment())     
		};
	}

	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	public boolean supports(CommentStatement statement, Database database) {
		return true;
	}

	public ValidationErrors validate(CommentStatement comment,
			Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("text", comment.getText());
        return validationErrors;
	}

}
