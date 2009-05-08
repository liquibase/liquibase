package liquibase.sqlgenerator;

import liquibase.database.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.AddUniqueConstraintStatement;
import liquibase.util.StringUtils;

class AddUniqueConstraintGenerator implements SqlGenerator<AddUniqueConstraintStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(AddUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase) 
        		&& !(database instanceof MSSQLDatabase)
        		&& !(database instanceof SybaseDatabase)
        		&& !(database instanceof SybaseASADatabase)
        ;
    }

    public ValidationErrors validate(AddUniqueConstraintStatement addUniqueConstraintStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("columnNames", addUniqueConstraintStatement.getColumnNames());
        validationErrors.checkRequiredField("tableName", addUniqueConstraintStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database) {
      String sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName()) + " UNIQUE (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
    	if (database instanceof InformixDatabase) {
    		sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT UNIQUE (" + database.escapeColumnNameList(statement.getColumnNames()) + ") CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
    	}

        if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON " + statement.getTablespace();
            } else if (database instanceof DB2Database
                || database instanceof SybaseASADatabase
                || database instanceof InformixDatabase) {
                ; //not supported
            } else {
                sql += " USING INDEX TABLESPACE " + statement.getTablespace();
            }
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };

    }
}
