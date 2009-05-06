package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.statement.FindForeignKeyConstraintsStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

public class FindForeignKeyConstraintsGeneratorPostgres implements SqlGenerator<FindForeignKeyConstraintsStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database) {
         StringBuilder sb = new StringBuilder();

            sb.append("SELECT ");
            sb.append("FK.TABLE_NAME as K_Table, ");
            sb.append("CU.COLUMN_NAME as FK_Column, ");
            sb.append("PK.TABLE_NAME as PK_Table, ");
            sb.append("PT.COLUMN_NAME as PK_Column,");
            sb.append("C.CONSTRAINT_NAME as Constraint_Name ");
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
            sb.append("WHERE      FK.TABLE_NAME='").append(statement.getBaseTableName()).append("'");

        return new Sql[] {
                new UnparsedSql(sb.toString())
        };
    }
}
