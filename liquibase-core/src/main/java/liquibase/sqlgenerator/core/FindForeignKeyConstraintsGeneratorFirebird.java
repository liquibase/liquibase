package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorFirebird extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof FirebirdDatabase;
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
        sb.append("TRIM(a.RDB$RELATION_NAME) as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
        sb.append("TRIM(b.RDB$FIELD_NAME) as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
        sb.append("TRIM(d.RDB$RELATION_NAME) as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
        sb.append("TRIM(e.RDB$FIELD_NAME) as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(",");
        sb.append("TRIM(a.RDB$CONSTRAINT_NAME) as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
        sb.append("FROM (((RDB$RELATION_CONSTRAINTS a LEFT OUTER JOIN RDB$INDEX_SEGMENTS b ON b.RDB$INDEX_NAME = a.RDB$INDEX_NAME) ");
        sb.append("LEFT OUTER JOIN RDB$INDICES c ON c.RDB$INDEX_NAME = a.RDB$INDEX_NAME) ");
        sb.append("LEFT OUTER JOIN RDB$RELATION_CONSTRAINTS d ON d.RDB$INDEX_NAME = c.RDB$FOREIGN_KEY AND d.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY') ");
        sb.append("LEFT OUTER JOIN RDB$INDEX_SEGMENTS e ON e.RDB$INDEX_NAME = d.RDB$INDEX_NAME AND e.RDB$FIELD_POSITION = b.RDB$FIELD_POSITION ");
        sb.append("WHERE a.RDB$RELATION_NAME='").append(statement.getBaseTableName()).append("' AND a.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY'");

        return new Sql[] { new UnparsedSql(sb.toString()) };
    }
}
