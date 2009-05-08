package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.StoredProcedureStatement;

public class StoredProcedureGenerator implements SqlGenerator<StoredProcedureStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(StoredProcedureStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(StoredProcedureStatement storedProcedureStatement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", storedProcedureStatement.getProcedureName());
        return validationErrors;
    }

    public Sql[] generateSql(StoredProcedureStatement statement, Database database) {
        StringBuffer string = new StringBuffer();
        string.append("exec (").append(statement.getProcedureName());
        for (String param : statement.getParameters()) {
            string.append(" ").append(param).append(",");
        }
        return new Sql[] { new UnparsedSql(string.toString().replaceFirst(",$", ")") )};

    }
}
