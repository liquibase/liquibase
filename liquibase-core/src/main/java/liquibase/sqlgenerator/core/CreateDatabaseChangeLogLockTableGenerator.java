package liquibase.sqlgenerator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.InsertStatement;

public class CreateDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogLockTableStatement> {

    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement createDatabaseChangeLogLockTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addPrimaryKeyColumn("ID", database.getDataTypeFactory().fromDescription("INT"), null, null, null, new NotNullConstraint())
                .addColumn("LOCKED", database.getDataTypeFactory().fromDescription("BOOLEAN"), null, new NotNullConstraint())
                .addColumn("LOCKGRANTED", database.getDataTypeFactory().fromDescription("DATETIME"))
                .addColumn("LOCKEDBY", database.getDataTypeFactory().fromDescription("VARCHAR(255)"));

        InsertStatement insertStatement = new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addColumnValue("ID", 1)
                .addColumnValue("LOCKED", Boolean.FALSE);

        List<Sql> sql = new ArrayList<Sql>();

        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database)));
        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(insertStatement, database)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
