package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.SetTableRemarksStatement;
import liquibase.sqlgenerator.SqlGenerator;

public class SetTableRemarksGenerator implements SqlGenerator<SetTableRemarksStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(SetTableRemarksStatement statement, Database database) {
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
