package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SetTableRemarksStatement;

public class SetTableRemarksGenerator extends AbstractSqlGenerator<SetTableRemarksStatement> {

    @Override
    public boolean supports(SetTableRemarksStatement statement, Database database) {
        return database instanceof MySQLDatabase || database instanceof OracleDatabase || database instanceof PostgresDatabase;
    }

    public ValidationErrors validate(SetTableRemarksStatement setTableRemarksStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", setTableRemarksStatement.getTableName());
        validationErrors.checkRequiredField("remarks", setTableRemarksStatement.getRemarks());
        return validationErrors;
    }

    public Sql[] generateSql(SetTableRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        String remarks = database.escapeStringForDatabase(statement.getRemarks());
        if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE "+database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())+" COMMENT = '"+remarks+"'";
        } else {
            sql = "COMMENT ON TABLE "+database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())+" IS '"+remarks+"'";
        }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
