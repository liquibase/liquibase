package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorOracle extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof OracleDatabase;
    }

    @Override
    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(FindForeignKeyConstraintsStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        CatalogAndSchema baseTableSchema = new CatalogAndSchema(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName()).customize(env.getTargetDatabase());

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

        return new Action[]{
                new UnparsedSql(sb.toString())
        };
    }
}