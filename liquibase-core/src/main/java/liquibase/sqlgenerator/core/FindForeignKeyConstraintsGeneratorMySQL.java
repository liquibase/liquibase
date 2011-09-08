package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorMySQL extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof MySQLDatabase;
    }

    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append("RC.TABLE_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
        sb.append("KCU.COLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
        sb.append("RC.REFERENCED_TABLE_NAME ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
        sb.append("KCU.REFERENCED_COLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(", ");
        sb.append("RC.CONSTRAINT_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
        sb.append("FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS RC,");
        sb.append("     INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU ");
        sb.append("WHERE RC.TABLE_NAME = KCU.TABLE_NAME ");
        sb.append("AND RC.CONSTRAINT_SCHEMA = KCU.CONSTRAINT_SCHEMA ");
        sb.append("AND RC.CONSTRAINT_NAME = KCU.CONSTRAINT_NAME ");
        sb.append("AND RC.TABLE_NAME = '").append(statement.getBaseTableName()).append("' ");
        try {
            sb.append("AND RC.CONSTRAINT_SCHEMA = '").append(database.convertRequestedSchemaToSchema(null)).append("'");
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return new Sql[]{
                new UnparsedSql(sb.toString())
        };
    }
}