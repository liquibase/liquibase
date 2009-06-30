package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class FindForeignKeyConstraintsGeneratorMSSQL implements SqlGenerator<FindForeignKeyConstraintsStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof MSSQLDatabase;
    }

    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT TOP 1");
        sb.append("OBJECT_NAME(f.parent_object_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
        sb.append("COL_NAME(fc.parent_object_id, fc.parent_column_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
        sb.append("OBJECT_NAME (f.referenced_object_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
        sb.append("COL_NAME(fc.referenced_object_id, fc.referenced_column_id) AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(",");
        sb.append("f.name AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
        sb.append("FROM sys.foreign_keys AS f ");
        sb.append("INNER JOIN sys.foreign_key_columns AS fc ");
        sb.append("ON f.OBJECT_ID = fc.constraint_object_id ");
        sb.append("WHERE OBJECT_NAME(f.parent_object_id) = '").append(statement.getBaseTableName()).append("'");

        return new Sql[]{
                new UnparsedSql(sb.toString())
        };
    }
}