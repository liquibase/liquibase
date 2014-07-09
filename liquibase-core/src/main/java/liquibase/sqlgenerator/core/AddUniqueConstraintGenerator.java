package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.util.StringUtils;

public class AddUniqueConstraintGenerator extends AbstractSqlGenerator<AddUniqueConstraintStatement> {

    @Override
    public boolean supports(AddUniqueConstraintStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return !(database instanceof SQLiteDatabase)
        		&& !(database instanceof MSSQLDatabase)
        		&& !(database instanceof SybaseDatabase)
        		&& !(database instanceof SybaseASADatabase)
        		&& !(database instanceof InformixDatabase)
        ;
    }

    @Override
    public ValidationErrors validate(AddUniqueConstraintStatement addUniqueConstraintStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("columnNames", addUniqueConstraintStatement.getColumnNames());
        validationErrors.checkRequiredField("tableName", addUniqueConstraintStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(AddUniqueConstraintStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

		String sql = null;
        if (statement.getConstraintName() == null) {
			sql = String.format("ALTER TABLE %s ADD UNIQUE (%s)"
					, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
					, database.escapeColumnNameList(statement.getColumnNames())
			);
		} else {
			sql = String.format("ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)"
					, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
					, database.escapeConstraintName(statement.getConstraintName())
					, database.escapeColumnNameList(statement.getColumnNames())
			);
		}
		if(database instanceof OracleDatabase) {
	        if (statement.isDeferrable() || statement.isInitiallyDeferred()) {
	            if (statement.isDeferrable()) {
	            	sql += " DEFERRABLE";
	            }

	            if (statement.isInitiallyDeferred()) {
	            	sql +=" INITIALLY DEFERRED";
	            }
	        }
            if (statement.isDisabled()) {
                sql +=" DISABLE";
            }
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

        return new Action[] {
                new UnparsedSql(sql)
        };

    }
}
