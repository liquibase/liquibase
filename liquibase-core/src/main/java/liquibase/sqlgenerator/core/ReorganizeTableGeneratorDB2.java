package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.ReorganizeTableStatement;

public class ReorganizeTableGeneratorDB2 extends AbstractSqlGenerator<ReorganizeTableStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(ReorganizeTableStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public ValidationErrors validate(ReorganizeTableStatement reorganizeTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reorganizeTableStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(ReorganizeTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
            if (database.getDatabaseMajorVersion() >= 9) {
                return new Sql[]{
                        new UnparsedSql("CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "')")
                };
            } else {
                return null;
            }
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }
}
