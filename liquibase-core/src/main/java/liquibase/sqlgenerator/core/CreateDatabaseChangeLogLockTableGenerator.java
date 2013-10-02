package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.InsertStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogLockTableStatement> {

    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement createDatabaseChangeLogLockTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addPrimaryKeyColumn("ID", DataTypeFactory.getInstance().fromDescription("INT"), null, null, null, new NotNullConstraint())
                .addColumn("LOCKED", DataTypeFactory.getInstance().fromDescription("BOOLEAN"), null, new NotNullConstraint())
                .addColumn("LOCKGRANTED", DataTypeFactory.getInstance().fromDescription("DATETIME"))
                .addColumn("LOCKEDBY", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)"));
        List<Sql> sql = new ArrayList<Sql>();

        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
