package liquibase.sqlgenerator.core;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.ReorganizeTableStatement;
import liquibase.sqlgenerator.SqlGenerator;

public class ReorganizeTableGeneratorDB2 implements SqlGenerator<ReorganizeTableStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(ReorganizeTableStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public ValidationErrors validate(ReorganizeTableStatement reorganizeTableStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reorganizeTableStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(ReorganizeTableStatement statement, Database database) {
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
