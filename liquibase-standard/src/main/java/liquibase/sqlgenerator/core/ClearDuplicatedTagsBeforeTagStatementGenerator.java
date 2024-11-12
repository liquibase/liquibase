package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.TagDatabaseStatement;
import liquibase.util.TagUtil;

public class ClearDuplicatedTagsBeforeTagStatementGenerator extends ChainableAbstractSqlGenerator<TagDatabaseStatement> {

    @Override
    public ValidationErrors validate(TagDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tag", statement.getTag());
        return validationErrors;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public Sql[] generateSql(TagDatabaseStatement statement, Database database) {
        String tag = statement.getTag();
        if (tag == null) {
            return EMPTY_SQL;
        }
        return TagUtil.buildClearDuplicatedTagSql(database, tag);
    }
}
