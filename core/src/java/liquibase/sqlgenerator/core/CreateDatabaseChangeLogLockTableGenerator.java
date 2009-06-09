package liquibase.sqlgenerator.core;

import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.NotNullConstraint;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorChain;

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
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getDefaultSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addPrimaryKeyColumn("ID", "INT", null, null, new NotNullConstraint())
                .addColumn("LOCKED", "BOOLEAN", new NotNullConstraint())
                .addColumn("LOCKGRANTED", "DATETIME")
                .addColumn("LOCKEDBY", "VARCHAR(255)");
        if (database instanceof MSSQLDatabase) {
        	createTableStatement.setSchemaName("dbo");
        }

        return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
    }
}
