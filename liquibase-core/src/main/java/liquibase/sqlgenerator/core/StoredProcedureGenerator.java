package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.StoredProcedureStatement;

public class StoredProcedureGenerator extends AbstractSqlGenerator<StoredProcedureStatement> {

    @Override
    public ValidationErrors validate(StoredProcedureStatement storedProcedureStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", storedProcedureStatement.getProcedureName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(StoredProcedureStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        StringBuilder string = new StringBuilder();
        string.append("exec ").append(statement.getProcedureName()).append("(");
        for (String param : statement.getParameters()) {
            string.append(" ").append(param).append(",");
        }
        String sql = string.toString().replaceFirst(",$", "")+")";

        if (env.getTargetDatabase() instanceof OracleDatabase) {
            sql = sql.replaceFirst("exec ", "BEGIN ").replaceFirst("\\)$", "); END;");
        }
        return new Action[] { new UnparsedSql(sql)};

    }
}
