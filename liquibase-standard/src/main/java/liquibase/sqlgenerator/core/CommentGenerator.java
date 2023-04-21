package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.SingleLineComment;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CommentStatement;

public class CommentGenerator extends AbstractSqlGenerator<CommentStatement> {

    @Override
    public Sql[] generateSql(CommentStatement comment, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new SingleLineComment(comment.getText(), database.getLineComment())     
        };
    }

    @Override
    public ValidationErrors validate(CommentStatement comment,
                                     Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("text", comment.getText());
        return validationErrors;
    }

}
