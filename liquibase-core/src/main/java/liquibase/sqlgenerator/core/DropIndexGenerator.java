package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.DropIndexStatement;
import liquibase.structure.core.Index;
import liquibase.util.StringUtils;

import java.util.List;

public class DropIndexGenerator extends AbstractSqlGenerator<DropIndexStatement> {

    @Override
    public ValidationErrors validate(DropIndexStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();

        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("indexName", statement.getIndexName());

        if (database instanceof MySQLDatabase || database instanceof MSSQLDatabase) {
                validationErrors.checkRequiredField("tableName", statement.getTableName());
        }

        return validationErrors;
    }

    @Override
    public Action[] generateActions(DropIndexStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
        if (associatedWith != null) {
            if (associatedWith.contains(Index.MARK_PRIMARY_KEY)|| associatedWith.contains(Index.MARK_UNIQUE_CONSTRAINT)) {
                return new Action[0];
            } else if (associatedWith.contains(Index.MARK_FOREIGN_KEY) ) {
                if (!(database instanceof OracleDatabase || database instanceof MSSQLDatabase)) {
                    return new Action[0];
                }
            }
        }

        String schemaName = statement.getTableSchemaName();
        
        if (database instanceof MySQLDatabase) {
            return new Action[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, null, statement.getIndexName()) + " ON " + database.escapeTableName(statement.getTableCatalogName(), schemaName, statement.getTableName())) };
        } else if (database instanceof MSSQLDatabase) {
            return new Action[] {new UnparsedSql("DROP INDEX " + database.escapeTableName(null, schemaName, statement.getTableName()) + "." + database.escapeIndexName(null, null, statement.getIndexName()))};
        } else if (database instanceof SybaseDatabase) {
            return new Action[]{new UnparsedSql("DROP INDEX " + statement.getTableName() + "." + statement.getIndexName())};
        } else if (database instanceof PostgresDatabase) {
            return new Action[]{new UnparsedSql("DROP INDEX " + database.escapeIndexName(statement.getTableCatalogName(),schemaName, statement.getIndexName()))};
		}

        return new Action[] {new UnparsedSql("DROP INDEX " + database.escapeIndexName(statement.getTableCatalogName(), schemaName, statement.getIndexName())) };
    }

}
