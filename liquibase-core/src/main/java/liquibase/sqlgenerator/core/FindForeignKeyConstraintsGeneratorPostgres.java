package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorPostgres extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    @Override
    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
         StringBuilder sb = new StringBuilder();

            sb.append("SELECT ");
            sb.append("FK.TABLE_NAME as \"").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append("\", ");
            sb.append("CU.COLUMN_NAME as \"").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append("\", ");
            sb.append("PK.TABLE_NAME as \"").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append("\", ");
            sb.append("PT.COLUMN_NAME as \"").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append("\", ");
            sb.append("C.CONSTRAINT_NAME as \"").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append("\" ");
            sb.append("FROM       INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS C ");
            sb.append("INNER JOIN  INFORMATION_SCHEMA.TABLE_CONSTRAINTS FK ON C.CONSTRAINT_NAME = FK.CONSTRAINT_NAME ");
            sb.append("INNER JOIN      INFORMATION_SCHEMA.TABLE_CONSTRAINTS PK ON C.UNIQUE_CONSTRAINT_NAME = PK.CONSTRAINT_NAME ");
            sb.append("INNER JOIN      INFORMATION_SCHEMA.KEY_COLUMN_USAGE CU ON C.CONSTRAINT_NAME = CU.CONSTRAINT_NAME ");
            sb.append("INNER JOIN  ( ");
            sb.append("  SELECT      i1.TABLE_NAME, i2.COLUMN_NAME ");
            sb.append("  FROM        INFORMATION_SCHEMA.TABLE_CONSTRAINTS i1 ");
            sb.append("  INNER JOIN      INFORMATION_SCHEMA.KEY_COLUMN_USAGE i2 ON i1.CONSTRAINT_NAME = i2.CONSTRAINT_NAME ");
            sb.append("  WHERE       i1.CONSTRAINT_TYPE = 'PRIMARY KEY' ");
            sb.append(") PT ON PT.TABLE_NAME = PK.TABLE_NAME ");
            sb.append("WHERE      lower(FK.TABLE_NAME)='").append(statement.getBaseTableName().toLowerCase()).append("'");

        return new Sql[] {
                new UnparsedSql(sb.toString())
        };
    }
}
