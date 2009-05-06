package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.statement.SetTableRemarksStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.ValidationErrors;

public class SetTableRemarksGenerator implements SqlGenerator<SetTableRemarksStatement>{
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(SetTableRemarksStatement statement, Database database) {
        return database instanceof MySQLDatabase || database instanceof OracleDatabase;
    }

    public ValidationErrors validate(SetTableRemarksStatement setTableRemarksStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", setTableRemarksStatement.getTableName());
        validationErrors.checkRequiredField("remarks", setTableRemarksStatement.getRemarks());
        return validationErrors;
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
