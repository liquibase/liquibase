package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateTableStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogLockTableStatement> {

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement createDatabaseChangeLogLockTableStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addPrimaryKeyColumn("ID", DataTypeFactory.getInstance().fromDescription("INT", database), null, null, null, new NotNullConstraint())
                .addColumn("LOCKED", DataTypeFactory.getInstance().fromDescription("BOOLEAN", database), null, new ColumnConstraint[]{new NotNullConstraint()})
                .addColumn("LOCKGRANTED", DataTypeFactory.getInstance().fromDescription("DATETIME", database))
                .addColumn("LOCKEDBY", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)", database));
        List<Sql> sql = new ArrayList<Sql>();

        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(createTableStatement, options)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
