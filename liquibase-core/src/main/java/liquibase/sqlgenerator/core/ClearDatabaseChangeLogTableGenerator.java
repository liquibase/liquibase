package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.ClearDatabaseChangeLogTableStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class ClearDatabaseChangeLogTableGenerator extends AbstractSqlGenerator<ClearDatabaseChangeLogTableStatement> {

    @Override
    public ValidationErrors validate(ClearDatabaseChangeLogTableStatement clearDatabaseChangeLogTableStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(ClearDatabaseChangeLogTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        String schemaName = statement.getSchemaName();
        if (schemaName == null) {
            schemaName = database.getLiquibaseSchemaName();
        }
        return new Action[] {
                new UnparsedSql("DELETE FROM " + database.escapeTableName(database.getLiquibaseCatalogName(), schemaName, database.getDatabaseChangeLogTableName())) };
    }

    protected Relation getAffectedTable(Database database, String schemaName) {
        return new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(database.getLiquibaseCatalogName(), schemaName);
    }
}
