package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorOracle extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    @Override
    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema baseTableSchema = new CatalogAndSchema(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName()).customize(database);

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append("BASE.TABLE_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
        sb.append("BCOLS.COLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
        sb.append("FRGN.TABLE_NAME ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
        sb.append("FCOLS.COLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(", ");
        sb.append("BASE.CONSTRAINT_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
        sb.append("FROM ALL_CONSTRAINTS BASE,");
        sb.append("     ALL_CONSTRAINTS FRGN,");
        sb.append("     ALL_CONS_COLUMNS BCOLS,");
        sb.append("     ALL_CONS_COLUMNS FCOLS ");
        sb.append("WHERE BASE.R_OWNER = FRGN.OWNER ");
        sb.append("AND BASE.R_CONSTRAINT_NAME = FRGN.CONSTRAINT_NAME ");
        sb.append("AND BASE.OWNER = BCOLS.OWNER ");
        sb.append("AND BASE.CONSTRAINT_NAME = BCOLS.CONSTRAINT_NAME ");
        sb.append("AND FRGN.OWNER = FCOLS.OWNER ");
        sb.append("AND FRGN.CONSTRAINT_NAME = FCOLS.CONSTRAINT_NAME ");
        sb.append("AND BASE.TABLE_NAME =  '").append(statement.getBaseTableName().toUpperCase()).append("' ");
        sb.append("AND BASE.CONSTRAINT_TYPE = 'R' ");
        sb.append("AND BASE.OWNER = '").append(baseTableSchema.getSchemaName()).append("'");

        return new Sql[]{
                new UnparsedSql(sb.toString())
        };
    }
}