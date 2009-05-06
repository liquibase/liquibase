package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.statement.SetTableRemarksStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;

public class SetTableRemarksGenerator implements SqlGenerator<SetTableRemarksStatement>{
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(SetTableRemarksStatement statement, Database database) {
        return database instanceof MySQLDatabase || database instanceof OracleDatabase;
    }

    public GeneratorValidationErrors validate(SetTableRemarksStatement setTableRemarksStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(SetTableRemarksStatement statement, Database database) {
        String sql;
        if (database instanceof OracleDatabase) {
            sql = "COMMENT ON TABLE "+database.escapeTableName(statement.getSchemaName(), statement.getTableName())+" IS '"+statement.getRemarks()+"'";
        } else {
            sql = "ALTER TABLE "+database.escapeTableName(statement.getSchemaName(), statement.getTableName())+" COMMENT = '"+statement.getRemarks()+"'";
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
