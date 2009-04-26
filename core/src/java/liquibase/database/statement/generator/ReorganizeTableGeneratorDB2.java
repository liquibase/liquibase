package liquibase.database.statement.generator;

import liquibase.database.statement.ReorganizeTableStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.Database;
import liquibase.database.DB2Database;
import liquibase.exception.JDBCException;

public class ReorganizeTableGeneratorDB2 implements SqlGenerator<ReorganizeTableStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(ReorganizeTableStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public GeneratorValidationErrors validate(ReorganizeTableStatement reorganizeTableStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(ReorganizeTableStatement statement, Database database) throws JDBCException {
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
