package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class ReorganizeTableGeneratorDB2 extends AbstractSqlGenerator<ReorganizeTableStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(ReorganizeTableStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof DB2Database;
    }

    @Override
    public ValidationErrors validate(ReorganizeTableStatement reorganizeTableStatement, ExecutionOptions options, ActionGeneratorChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reorganizeTableStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(ReorganizeTableStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
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

    protected Relation getAffectedTable(ReorganizeTableStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
