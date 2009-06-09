package liquibase.sqlgenerator.core;

import liquibase.database.*;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.AddPrimaryKeyStatement;
import liquibase.util.StringUtils;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class AddPrimaryKeyGenerator implements SqlGenerator<AddPrimaryKeyStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(AddPrimaryKeyStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    public ValidationErrors validate(AddPrimaryKeyStatement addPrimaryKeyStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("columnNames", addPrimaryKeyStatement.getColumnNames());
        validationErrors.checkRequiredField("tableName", addPrimaryKeyStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(AddPrimaryKeyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        if (statement.getConstraintName() == null  || database instanceof MySQLDatabase || database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
        } else if (database instanceof InformixDatabase) {
        	sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ") CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName()) + " PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
        }

        if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON "+statement.getTablespace();
            } else if (database instanceof DB2Database || database instanceof SybaseASADatabase) {
                ; //not supported
            } else {
                sql += " USING INDEX TABLESPACE "+statement.getTablespace();
            }
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
