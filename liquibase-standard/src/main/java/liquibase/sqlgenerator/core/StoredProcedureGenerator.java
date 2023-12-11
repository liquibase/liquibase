package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.StoredProcedureStatement;

public class StoredProcedureGenerator extends AbstractSqlGenerator<StoredProcedureStatement> {

    @Override
    public ValidationErrors validate(StoredProcedureStatement storedProcedureStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", storedProcedureStatement.getProcedureName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(StoredProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder string = new StringBuilder();
        string.append("exec ").append(statement.getProcedureName()).append("(");
        for (String param : statement.getParameters()) {
            string.append(" ").append(param).append(",");
        }
        String sql = string.toString().replaceFirst(",$", "")+")";

        if (database instanceof OracleDatabase) {
            sql = sql.replaceFirst("exec ", "BEGIN ").replaceFirst("\\)$", "); END;");
        }
        return new Sql[] { new UnparsedSql(sql)};

    }
}
