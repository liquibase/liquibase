package liquibase.statement.generator;

import liquibase.database.*;
import liquibase.statement.AddPrimaryKeyStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.util.StringUtils;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

public class AddPrimaryKeyGenerator implements SqlGenerator<AddPrimaryKeyStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(AddPrimaryKeyStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    public ValidationErrors validate(AddPrimaryKeyStatement addPrimaryKeyStatement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(AddPrimaryKeyStatement statement, Database database) {
        String sql;
        if (statement.getConstraintName() == null  || database instanceof MySQLDatabase || database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
        } else if (database instanceof InformixDatabase) {
        	sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ") CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName()) + " PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
        }

        if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase ) {
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
