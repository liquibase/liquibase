package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SelectFromDatabaseChangeLogGenerator extends AbstractSqlGenerator<SelectFromDatabaseChangeLogStatement> {

    @Override
    public ValidationErrors validate(SelectFromDatabaseChangeLogStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("columnToSelect", statement.getColumnsToSelect());

        return errors;
    }

    @Override
    public Action[] generateActions(SelectFromDatabaseChangeLogStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        List<String> columnsToSelect = Arrays.asList(statement.getColumnsToSelect());
        for (int i=0; i<columnsToSelect.size(); i++) {
            columnsToSelect.set(i, database.escapeColumnName(null, null, null, columnsToSelect.get(i)));
        }
        String sql = "SELECT " + StringUtils.join(columnsToSelect, ",").toUpperCase() + " FROM " +
                database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());

        SelectFromDatabaseChangeLogStatement.WhereClause whereClause = statement.getWhereClause();
        if (whereClause != null) {
            if (whereClause instanceof SelectFromDatabaseChangeLogStatement.ByTag) {
                sql += " WHERE "+ database.escapeColumnName(null, null, null, "TAG")+"='" + ((SelectFromDatabaseChangeLogStatement.ByTag) whereClause).getTagName() + "'";
            } else if (whereClause instanceof SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum) {
                    sql += " WHERE MD5SUM IS NOT NULL";
            } else {
                throw new UnexpectedLiquibaseException("Unknown where clause type: " + whereClause.getClass().getName());
            }
        }

        if (statement.getOrderBy() != null && statement.getOrderBy().length > 0) {
            sql += " ORDER BY "+StringUtils.join(statement.getOrderBy(), ", ").toUpperCase();
        }

        return new Action[]{
                new UnparsedSql(sql)
        };
    }
}