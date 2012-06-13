package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.StoredProcedureStatement;

public class StoredProcedureGenerator extends AbstractSqlGenerator<StoredProcedureStatement> {

    public ValidationErrors validate(StoredProcedureStatement storedProcedureStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", storedProcedureStatement.getProcedureName());
        return validationErrors;
    }

    public Sql[] generateSql(StoredProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer string = new StringBuffer();
        string.append("exec (").append(statement.getProcedureName());
        for (String param : statement.getParameters()) {
            string.append(" ").append(param).append(",");
        }
        return new Sql[] { new UnparsedSql(string.toString().replaceFirst(",$", ")") )};

    }
}
