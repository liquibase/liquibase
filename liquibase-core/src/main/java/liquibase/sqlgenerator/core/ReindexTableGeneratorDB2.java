package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.ReindexStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class ReindexTableGeneratorDB2 extends AbstractSqlGenerator<ReindexStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(ReindexStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof DB2Database;
    }

    @Override
    public ValidationErrors validate(ReindexStatement reindexStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reindexStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(ReindexStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();
        try {
            if (database.getDatabaseMajorVersion() >= 9) {
                return new Action[]{
                        new UnparsedSql("CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "')")
                };
            } else {
                return null;
            }
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    protected Relation getAffectedTable(ReindexStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
