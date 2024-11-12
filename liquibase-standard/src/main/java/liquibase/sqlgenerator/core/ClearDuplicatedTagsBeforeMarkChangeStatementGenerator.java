package liquibase.sqlgenerator.core;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.util.TagUtil;

public class ClearDuplicatedTagsBeforeMarkChangeStatementGenerator extends ChainableAbstractSqlGenerator<MarkChangeSetRanStatement> {

    @Override
    public ValidationErrors validate(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(MarkChangeSetRanStatement statement, Database database) {
        if (statement.getExecType().equals(ChangeSet.ExecType.FAILED)
                || statement.getExecType().equals(ChangeSet.ExecType.SKIPPED)) {
            return EMPTY_SQL;
        }
        String tag = TagUtil.getTagFromChangeset(statement.getChangeSet());
        if (tag == null) {
            return EMPTY_SQL;
        }

        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            return TagUtil.buildClearDuplicatedTagSql(database, tag);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
}
