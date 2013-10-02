package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorDB2 extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
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