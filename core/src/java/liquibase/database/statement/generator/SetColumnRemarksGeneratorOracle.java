package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.database.statement.SetColumnRemarksStatement;
import liquibase.exception.JDBCException;

public class SetColumnRemarksGeneratorOracle implements SqlGenerator<SetColumnRemarksStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(SetColumnRemarksStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    public GeneratorValidationErrors validate(SetColumnRemarksStatement setColumnRemarksStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(SetColumnRemarksStatement statement, Database database) throws JDBCException {
        return new Sql[] {
                new UnparsedSql("COMMENT ON COLUMN "+database.escapeTableName(statement.getSchemaName(), statement.getTableName())+"."+database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName())+" IS '"+statement.getRemarks()+"'")
        };
    }
}
