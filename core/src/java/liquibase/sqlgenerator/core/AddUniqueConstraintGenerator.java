package liquibase.sqlgenerator.core;

import liquibase.database.*;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.util.StringUtils;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class AddUniqueConstraintGenerator implements SqlGenerator<AddUniqueConstraintStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(AddUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase)
        		&& !(database instanceof MSSQLDatabase)
        		&& !(database instanceof SybaseDatabase)
        		&& !(database instanceof SybaseASADatabase)
        		&& !(database instanceof InformixDatabase)
        ;
    }

    public ValidationErrors validate(AddUniqueConstraintStatement addUniqueConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("columnNames", addUniqueConstraintStatement.getColumnNames());
        validationErrors.checkRequiredField("tableName", addUniqueConstraintStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		
		String sql = null;
		if (statement.getConstraintName() == null) {
			sql = String.format("ALTER TABLE %s ADD UNIQUE (%s)"
					, database.escapeTableName(statement.getSchemaName(), statement.getTableName())
					, database.escapeColumnNameList(statement.getColumnNames())
			);
		} else {
			sql = String.format("ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)"
					, database.escapeTableName(statement.getSchemaName(), statement.getTableName())
					, database.escapeConstraintName(statement.getConstraintName())
					, database.escapeColumnNameList(statement.getColumnNames())
			);
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
