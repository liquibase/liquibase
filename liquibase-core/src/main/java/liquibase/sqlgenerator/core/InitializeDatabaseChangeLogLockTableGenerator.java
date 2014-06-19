package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InitializeDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<InitializeDatabaseChangeLogLockTableStatement> {

    @Override
    public ValidationErrors validate(InitializeDatabaseChangeLogLockTableStatement initializeDatabaseChangeLogLockTableStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(InitializeDatabaseChangeLogLockTableStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        DeleteStatement deleteStatement = new DeleteStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
        InsertStatement insertStatement = new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addColumnValue("ID", 1)
                .addColumnValue("LOCKED", Boolean.FALSE);

        List<Sql> sql = new ArrayList<Sql>();

        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(deleteStatement, options)));
        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(insertStatement, options)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
