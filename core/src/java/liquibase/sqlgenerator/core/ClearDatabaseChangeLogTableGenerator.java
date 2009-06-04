package liquibase.sqlgenerator.core;

import liquibase.statement.ClearDatabaseChangeLogTableStatement;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class ClearDatabaseChangeLogTableGenerator implements SqlGenerator<ClearDatabaseChangeLogTableStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(ClearDatabaseChangeLogTableStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(ClearDatabaseChangeLogTableStatement clearDatabaseChangeLogTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(ClearDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
        	String schemaName = null;
        	if (database.isPeculiarLiquibaseSchema()) {
        		schemaName = database.getLiquibaseSchemaName();
        	} else {
        		schemaName = database.convertRequestedSchemaToSchema(null);
        	}
            return new Sql[] {
                    new UnparsedSql("DELETE FROM " + database.escapeTableName(schemaName, database.getDatabaseChangeLogTableName()))
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
