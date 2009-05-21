package liquibase.sqlgenerator;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.NotNullConstraint;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;

public class CreateDatabaseChangeLogLockTableGenerator implements SqlGenerator<CreateDatabaseChangeLogLockTableStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(CreateDatabaseChangeLogLockTableStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement createDatabaseChangeLogLockTableStatement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database) {
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
