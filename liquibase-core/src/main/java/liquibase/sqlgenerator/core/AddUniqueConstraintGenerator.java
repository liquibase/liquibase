package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.util.StringUtils;

public class AddUniqueConstraintGenerator extends AbstractSqlGenerator<AddUniqueConstraintStatement> {

    @Override
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

        return new Sql[] {
                new UnparsedSql(sql)
        };

    }
}
