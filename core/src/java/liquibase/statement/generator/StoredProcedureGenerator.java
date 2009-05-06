package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.statement.StoredProcedureStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;

public class StoredProcedureGenerator implements SqlGenerator<StoredProcedureStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(StoredProcedureStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(StoredProcedureStatement storedProcedureStatement, Database database) {
        return new GeneratorValidationErrors();
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
