package liquibase.sqlgenerator.core;

import liquibase.statement.SelectFromDatabaseChangeLogLockStatement;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.util.StringUtils;
import liquibase.sqlgenerator.SqlGenerator;

public class SelectFromDatabaseChangeLogLockGenerator implements SqlGenerator<SelectFromDatabaseChangeLogLockStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(SelectFromDatabaseChangeLogLockStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(SelectFromDatabaseChangeLogLockStatement statement, Database database) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("columnToSelect", statement.getColumnsToSelect());

        return errors;
    }

    public Sql[] generateSql(SelectFromDatabaseChangeLogLockStatement statement, Database database) {
    	String liquibaseSchema = null;
   		liquibaseSchema = database.getLiquibaseSchemaName();
        String sql = "SELECT "+ StringUtils.join(statement.getColumnsToSelect(), ",")+" FROM " +
                database.escapeTableName(liquibaseSchema, database.getDatabaseChangeLogLockTableName()) +
                " WHERE " + database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogLockTableName(), "ID") + "=1";

        if (database instanceof OracleDatabase) {
            sql += " for update";
        }
        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
