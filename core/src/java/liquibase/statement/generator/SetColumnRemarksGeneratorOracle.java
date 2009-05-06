package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.statement.SetColumnRemarksStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;

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

    public Sql[] generateSql(SetColumnRemarksStatement statement, Database database) {
        return new Sql[] {
                new UnparsedSql("COMMENT ON COLUMN "+database.escapeTableName(statement.getSchemaName(), statement.getTableName())+"."+database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName())+" IS '"+statement.getRemarks()+"'")
        };
    }
}
