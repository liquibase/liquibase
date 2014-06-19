package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.ClearDatabaseChangeLogTableStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class ClearDatabaseChangeLogTableGenerator extends AbstractSqlGenerator<ClearDatabaseChangeLogTableStatement> {

    @Override
    public ValidationErrors validate(ClearDatabaseChangeLogTableStatement clearDatabaseChangeLogTableStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(ClearDatabaseChangeLogTableStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        String schemaName = statement.getSchemaName();
        if (schemaName == null) {
            schemaName = database.getLiquibaseSchemaName();
        }
        return new Sql[] {
                new UnparsedSql("DELETE FROM " + database.escapeTableName(database.getLiquibaseCatalogName(), schemaName, database.getDatabaseChangeLogTableName())) };
    }

    protected Relation getAffectedTable(Database database, String schemaName) {
        return new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(database.getLiquibaseCatalogName(), schemaName);
    }
}
