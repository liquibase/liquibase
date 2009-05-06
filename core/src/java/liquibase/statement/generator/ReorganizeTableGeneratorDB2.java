package liquibase.statement.generator;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;
import liquibase.statement.ReorganizeTableStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;

public class ReorganizeTableGeneratorDB2 implements SqlGenerator<ReorganizeTableStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(ReorganizeTableStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public ValidationErrors validate(ReorganizeTableStatement reorganizeTableStatement, Database database) {
        return new ValidationErrors();
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
