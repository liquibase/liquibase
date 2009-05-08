package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.SetColumnRemarksStatement;

public class SetColumnRemarksGeneratorOracle implements SqlGenerator<SetColumnRemarksStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(SetColumnRemarksStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    public ValidationErrors validate(SetColumnRemarksStatement setColumnRemarksStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", setColumnRemarksStatement.getTableName());
        validationErrors.checkRequiredField("columnName", setColumnRemarksStatement.getColumnName());
        validationErrors.checkRequiredField("remarks", setColumnRemarksStatement.getRemarks());
        return validationErrors;
    }

    public Sql[] generateSql(SetColumnRemarksStatement statement, Database database) {
        return new Sql[] {
                new UnparsedSql("COMMENT ON COLUMN "+database.escapeTableName(statement.getSchemaName(), statement.getTableName())+"."+database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName())+" IS '"+statement.getRemarks()+"'")
        };
    }
}
