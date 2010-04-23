package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.database.structure.Index;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropIndexStatement;
import liquibase.util.StringUtils;

import java.util.List;

public class DropIndexGenerator implements SqlGenerator<DropIndexStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(DropIndexStatement statement, Database database) {
        return true;
    }

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
            return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, statement.getIndexName()) + " ON " + database.escapeTableName(schemaName, statement.getTableName())) };
        } else if (database instanceof MSSQLDatabase) {
            return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeTableName(schemaName, statement.getTableName()) + "." + database.escapeIndexName(null, statement.getIndexName())) };
        }

        return new Sql[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(schemaName, statement.getIndexName())) };
    }
}
