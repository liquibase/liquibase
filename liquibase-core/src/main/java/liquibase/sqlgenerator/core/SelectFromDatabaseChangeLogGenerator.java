package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SelectFromDatabaseChangeLogGenerator extends AbstractSqlGenerator<SelectFromDatabaseChangeLogStatement> {

    @Override
    public ValidationErrors validate(SelectFromDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("columnToSelect", statement.getColumnsToSelect());

        return errors;
    }

    @Override
    public Sql[] generateSql(SelectFromDatabaseChangeLogStatement statement, final Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<ColumnConfig> columnsToSelect = Arrays.asList(statement.getColumnsToSelect());
        String sql = "SELECT " + StringUtils.join(columnsToSelect, ",", new StringUtils.StringUtilsFormatter<ColumnConfig>() {
            @Override
            public String toString(ColumnConfig column) {
                if (column.getComputed() != null && column.getComputed()) {
                    return column.getName();
                } else {
                    return database.escapeColumnName(null, null, null, column.getName());
                }
            }
        }).toUpperCase() + " FROM " +
                database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());

        SelectFromDatabaseChangeLogStatement.WhereClause whereClause = statement.getWhereClause();
        if (whereClause != null) {
            if (whereClause instanceof SelectFromDatabaseChangeLogStatement.ByTag) {
                sql += " WHERE "+database.escapeColumnName(null, null, null, "TAG")+"='" + ((SelectFromDatabaseChangeLogStatement.ByTag) whereClause).getTagName() + "'";
            } else if (whereClause instanceof SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum) {
                    sql += " WHERE MD5SUM IS NOT NULL";
            } else {
                throw new UnexpectedLiquibaseException("Unknown where clause type: " + whereClause.getClass().getName());
            }
        }

        if (statement.getOrderByColumns() != null && statement.getOrderByColumns().length > 0) {
            sql += " ORDER BY "+StringUtils.join(statement.getOrderByColumns(), ", ").toUpperCase();
        }

        return new Sql[]{
                new UnparsedSql(sql)
        };
    }
}