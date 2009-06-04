package liquibase.sqlgenerator.core;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.ReorganizeTableStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class ReorganizeTableGeneratorDB2 implements SqlGenerator<ReorganizeTableStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

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
                        new UnparsedSql("CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + "')")
                };
            } else {
                return null;
            }
        } catch (JDBCException e) {
            throw new RuntimeException(e);
        }
    }
}
