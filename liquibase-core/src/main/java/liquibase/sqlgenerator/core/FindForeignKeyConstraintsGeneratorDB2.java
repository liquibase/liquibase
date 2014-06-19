package liquibase.sqlgenerator.core;

import liquibase.database.core.DB2Database;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorDB2 extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof DB2Database;
    }

    @Override
    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append("TABNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
        sb.append("PK_COLNAMES as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
        sb.append("REFTABNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
        sb.append("FK_COLNAMES as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(",");
        sb.append("CONSTNAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
        sb.append("FROM SYSCAT.REFERENCES ");
        sb.append("WHERE TABNAME='").append(statement.getBaseTableName()).append("'");

        return new Sql[]{
                new UnparsedSql(sb.toString())
        };
    }
}