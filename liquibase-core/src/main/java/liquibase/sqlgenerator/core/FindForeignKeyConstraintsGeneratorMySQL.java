package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
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

    @Override
    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName()).customize(database);

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
        sb.append("AND RC.CONSTRAINT_SCHEMA = '").append(schema.getCatalogName()).append("'");
        sb.append("AND KCU.TABLE_SCHEMA = '").append(schema.getCatalogName()).append("'");
        return new Sql[]{
                new UnparsedSql(sb.toString())
        };
    }
}