package liquibase.sqlgenerator.core;

import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.structure.Index;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropIndexStatement;
import liquibase.util.StringUtils;

public class DropIndexGenerator extends AbstractSqlGenerator<DropIndexStatement> {

    public ValidationErrors validate(DropIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("indexName", statement.getIndexName());

        if (database instanceof MySQLDatabase || database instanceof MSSQLDatabase) {
                validationErrors.checkRequiredField("tableName", statement.getTableName());
        }

        return validationErrors;
    }

    public Sql[] generateSql(DropIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
        if (associatedWith != null) {
            if (associatedWith.contains(Index.MARK_PRIMARY_KEY)|| associatedWith.contains(Index.MARK_UNIQUE_CONSTRAINT)) {
                return new Sql[0];
            } else if (associatedWith.contains(Index.MARK_FOREIGN_KEY) ) {
                if (!(database instanceof OracleDatabase || database instanceof MSSQLDatabase)) {
                    return new Sql[0];
                }
            }
        }

        String schemaName = statement.getTableSchemaName();
        
        if (database instanceof MySQLDatabase) {
            return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, null, statement.getIndexName()) + " ON " + database.escapeTableName(statement.getTableCatalogName(), schemaName, statement.getTableName())) };
        } else if (database instanceof MSSQLDatabase) {
            return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeTableName(statement.getTableCatalogName(), schemaName, statement.getTableName()) + "." + database.escapeIndexName(null, null, statement.getIndexName())) };
        } else if (database instanceof PostgresDatabase) {
			return new Sql[]{new UnparsedSql("DROP INDEX " + database.escapeIndexName(statement.getTableCatalogName(),schemaName, statement.getIndexName()))};
		}

        return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(statement.getTableCatalogName(), schemaName, statement.getIndexName())) };
    }
}
