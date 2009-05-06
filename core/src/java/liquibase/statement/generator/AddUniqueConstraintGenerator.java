package liquibase.statement.generator;

import liquibase.database.*;
import liquibase.statement.AddUniqueConstraintStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.util.StringUtils;

public class AddUniqueConstraintGenerator implements SqlGenerator<AddUniqueConstraintStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(AddUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase) 
        		&& !(database instanceof MSSQLDatabase)
        		&& !(database instanceof SybaseDatabase)
        		&& !(database instanceof SybaseASADatabase)
        ;
    }

    public GeneratorValidationErrors validate(AddUniqueConstraintStatement addUniqueConstraintStatement, Database database) {
        return new GeneratorValidationErrors();
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
