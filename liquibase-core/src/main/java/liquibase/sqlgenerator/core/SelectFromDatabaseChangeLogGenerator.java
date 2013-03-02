package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;
import liquibase.structure.core.Column;
import liquibase.util.StringUtils;

public class SelectFromDatabaseChangeLogGenerator extends AbstractSqlGenerator<SelectFromDatabaseChangeLogStatement> {

    public ValidationErrors validate(SelectFromDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("columnToSelect", statement.getColumnsToSelect());

        return errors;
    }

    public Sql[] generateSql(SelectFromDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0 ; i < statement.getColumnsToSelect().length; i ++) {
            SelectFromDatabaseChangeLogStatement.SelectableColumn column = statement.getColumnsToSelect()[i];
            String correctedColumnName = database.escapeObjectName(column.getColumnName(), Column.class);
            if (column.getColumnFunction() != null) {
                correctedColumnName = column.getColumnFunction() + "(" + correctedColumnName + ")";
            }
            sql.append(correctedColumnName);
            if (i != statement.getColumnsToSelect().length -1) {
                sql.append(", ");
            }
        }
        sql.append(" FROM " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),
                database.getDatabaseChangeLogTableName()));

        SelectFromDatabaseChangeLogStatement.WhereClause whereClause = statement.getWhereClause();
        if (whereClause != null) {
            if (whereClause instanceof SelectFromDatabaseChangeLogStatement.ByTag) {
                sql.append(" WHERE " + database.escapeObjectName("TAG", Column.class))
                        .append(" ='").append(((SelectFromDatabaseChangeLogStatement.ByTag) whereClause).getTagName())
                        .append("'");
            } else if (whereClause instanceof SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum) {
                    sql.append(" WHERE ").append(database.escapeObjectName("MD5SUM", Column.class)).append(" IS NOT NULL");
            } else {
                throw new UnexpectedLiquibaseException("Unknown where clause type: " + whereClause.getClass().getName());
            }
        }

        if (statement.getOrderByColumns() != null && statement.getOrderByColumns().length > 0) {
            sql.append(" ORDER BY ");
            for (int i = 0; i < statement.getOrderByColumns().length; i ++) {
                SelectFromDatabaseChangeLogStatement.OrderByColumn orderByColumn = statement.getOrderByColumns()[i];
                sql.append( database.escapeObjectName(orderByColumn.getColumnName(), Column.class)).append(" ")
                        .append(orderByColumn.getOrderByClause());
                if (i != statement.getOrderByColumns().length - 1) {
                    sql.append(", ");
                }
            }
        }

        return new Sql[]{
                new UnparsedSql(sql.toString())
        };
    }
}