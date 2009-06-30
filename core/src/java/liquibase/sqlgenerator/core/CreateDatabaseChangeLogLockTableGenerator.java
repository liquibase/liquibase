package liquibase.sqlgenerator.core;

import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.NotNullConstraint;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class CreateDatabaseChangeLogLockTableGenerator implements SqlGenerator<CreateDatabaseChangeLogLockTableStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(CreateDatabaseChangeLogLockTableStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement createDatabaseChangeLogLockTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addPrimaryKeyColumn("ID", "INT", null, null, new NotNullConstraint())
                .addColumn("LOCKED", "BOOLEAN", new NotNullConstraint())
                .addColumn("LOCKGRANTED", "DATETIME")
                .addColumn("LOCKEDBY", "VARCHAR(255)");

        InsertStatement insertStatement = new InsertStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addColumnValue("ID", 1)
                .addColumnValue("LOCKED", Boolean.FALSE);

        List<Sql> sql = new ArrayList<Sql>();

        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database)));
        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(insertStatement, database)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
